// Out includs
#include "mqtt_client.h"
#include "constants.h"
#include "log.h"
#include "mqtt_callbacks.h"
#include "mqtt_map.h"

// Mosquitto includes
#include <mosquitto.h>
#include <mqtt_protocol.h>

#include <errno.h>
#include <fcntl.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#ifndef WIN32
#include <sys/time.h>
#include <time.h>
#else
#include <process.h>
#include <winsock2.h>
#define snprintf sprintf_s
#endif

/// @brief External definition of component callbacks
///
/// @param set_callback The function pointer to the callback function which is
/// called when a component slot has changed.
extern void sys_component_on_change(void (*set_callback)(SedonaVM *vm,
                                                         uint8_t *comp,
                                                         void *slot));

static void mqtt_client_set_status(enum MqttConnectionState);
static MQTT_SLOT_KEY_TYPE
MagolvesMqtt_CbcMiddlewareService_computeSlotKey(SedonaVM *vm, uint8_t *self,
                                                 void *slot);

static void changeListener(SedonaVM *vm, uint8_t *comp, void *slot);
static void renderPayload(char *buffer, size_t *len, uint8_t *self,
                          uint16_t offset, uint16_t tid);
static void renderPayloadAsJson(char *buffer, uint8_t *self, uint16_t offset,
                                uint16_t tid);

static volatile int status = STATUS_CONNECTING;
#define MAX_PATH_LENGTH 512
/// @brief Common path for assembling the topic during registration
static char MQTT_PATH_BUFFER[MAX_PATH_LENGTH + 1];
#define MAX_JSON_LENGTH 512
/// @brief Common buffer for JSON string rendering
static char JSON_BUFFER[MAX_JSON_LENGTH + 1];

///////////////////////////////////////////////////////
// Internal functions
///////////////////////////////////////////////////////

void mqtt_client_set_status(enum MqttConnectionState new_status) {
  status = new_status;
}

///////////////////////////////////////////////////////
// Native Method Slots
///////////////////////////////////////////////////////

/// @brief Starts the MQTT session expecting the following content
/// - param[0]: Host name or IP (const char*)
/// - param[1]: Port (int)
/// - param[2]: Client name (const char* - may be NULL to use a generated ID
/// string)
/// - param[3]: User name (const char* - may be NULL if no auth s required)
/// - param[4]: Password (const char*)
///
/// @param vm the VM instance
/// @param params the paramater array
/// @return Cell trueCell, if successful
Cell MagolvesMqtt_CbcMiddlewareService_startSession(SedonaVM *vm,
                                                    Cell *params) {
  char *host = params[0].aval;
  int32_t port = params[1].ival;
  char *clientid = params[2].aval;
  char *username = params[3].aval;
  char *password = params[4].aval;

  struct mosquitto *mosq = NULL;
  int rc;

  mosquitto_lib_init();

  mosq = mosquitto_new(MW_CLIENT_NAME, MW_CLEAN_SESSION, NULL);
  if (!mosq) {
    mqtt_client_set_status(STATUS_INTERNAL_ERROR);
  } else {
    // Set MQTT protocol version to 5
    mosquitto_int_option(mosq, MOSQ_OPT_PROTOCOL_VERSION, MQTT_PROTOCOL_V5);
    // Specify example last will (must be done before connect)
    mosquitto_will_set_v5(mosq, "last/Will", 0, NULL, 0, false, NULL);

    mosquitto_log_callback_set(mosq, mqtt_log_callback);
    mosquitto_connect_v5_callback_set(mosq, mqtt_connect_callback_v5);
    mosquitto_disconnect_v5_callback_set(mosq, mqtt_disconnect_callback_v5);
    mosquitto_message_v5_callback_set(mosq, mqtt_message_v5);
    mosquitto_message_callback_set(mosq, mqtt_message);
    mosquitto_subscribe_v5_callback_set(mosq, mqtt_subscribe_callback_v5);
    mosquitto_subscribe_callback_set(mosq, mqtt_subscribe_callback);

    rc = mosquitto_loop_start(mosq);

    if (rc != MOSQ_ERR_SUCCESS) {
      if (rc == MOSQ_ERR_INVAL) {
        printf("mosquitto_loop_start: Invalid parameters %s (%d)\n", host,
               port);
      } else {
        printf("mosquitto_loop_start: Illegal call (rc = %d)\n", rc);
      }
      mqtt_client_set_status(STATUS_DOWN);
    } else {
      mqtt_client_set_status(STATUS_CONNECTING);
      rc = mosquitto_connect_async(mosq, host, port, 60);

      if (rc != MOSQ_ERR_SUCCESS) {
        if (rc == MOSQ_ERR_INVAL) {
          printf("mosquitto_connect_async: Invalid parameters %s (%d)\n", host,
                 port);
        } else {
          printf("mosquitto_connect_async: Illegal call (rc = %d)\n", rc);
        }
        mqtt_client_set_status(STATUS_DOWN);
      } else {
        mqtt_client_set_status(STATUS_CONNECTED);
        // Install component change listener
        sys_component_on_change(changeListener);
      }
    }
  }

  Cell result;
  result.aval = (void *)mosq;
  return result;
}

/// @brief Stop the MQTT session.
///
/// @param vm the VM instance
/// @param params the paramater array
/// @return Cell trueCell, if successful
Cell MagolvesMqtt_CbcMiddlewareService_stopSession(SedonaVM *vm, Cell *params) {
  struct mosquitto *mosq = (struct mosquitto *)params[0].aval;
  if (!mosq)
    return falseCell;

  // Remove component change listener
  sys_component_on_change(NULL);

  mqtt_client_set_status(STATUS_DISCONNECTING);
  mosquitto_disconnect_v5(mosq, MQTT_RC_DISCONNECT_WITH_WILL_MSG, NULL);
  mqtt_client_set_status(STATUS_DISCONNECTED);

  return trueCell;
}

/// @brief Check if MQTT session is alive.
///
/// @param vm the VM instance
/// @param params the paramater array
/// @return Cell trueCell, if session is alive/connected
Cell MagolvesMqtt_CbcMiddlewareService_isSessionLive(SedonaVM *vm,
                                                     Cell *params) {
  struct mosquitto *mosq = (struct mosquitto *)params[0].aval;
  if (!mosq)
    return falseCell;

  return (status == STATUS_CONNECTED) ? trueCell : falseCell;
}

/// @brief Get the connection state.
///
/// @param vm the VM instance
/// @param params the paramater array
/// @return Cell the connection status in ival
Cell MagolvesMqtt_CbcMiddlewareService_getStatus(SedonaVM *vm, Cell *params) {
  Cell result = {status};
  return result;
}

/// @brief Executes a single MQTT loop. Can only be used if mosquitto is
/// operated synchronously.
///
/// @param vm the VM instance
/// @param params the paramater array
/// @return Cell the result
Cell MagolvesMqtt_CbcMiddlewareService_execute(SedonaVM *vm, Cell *params) {
  struct mosquitto *mosq = (struct mosquitto *)params[0].aval;

  // Parameters
  // - mosq	a valid mosquitto instance.
  // - timeout	Maximum number of milliseconds to wait for network activity in
  // the select() call before timing out.  Set to 0 for instant return.  Set
  // negative to use the default of 1000ms.
  // - max_packets	this parameter is currently unused and should be set to
  // 1 for future compatibility.
  mosquitto_loop(mosq, -1, 1);
  return trueCell;
}

Cell MagolvesMqtt_CbcMiddlewareService_exportMonitor(SedonaVM *vm,
                                                     Cell *params) {
  MagolvesMqtt_CbcMiddlewareService_export(vm, params, EXPORT_MONITOR);
}

Cell MagolvesMqtt_CbcMiddlewareService_exportParameter(SedonaVM *vm,
                                                       Cell *params) {
  MagolvesMqtt_CbcMiddlewareService_export(vm, params, EXPORT_PARAMETER);
}

Cell MagolvesMqtt_CbcMiddlewareService_exportAction(SedonaVM *vm,
                                                    Cell *params) {
  MagolvesMqtt_CbcMiddlewareService_export(vm, params, EXPORT_ACTION);
}

Cell MagolvesMqtt_CbcMiddlewareService_export(SedonaVM *vm, Cell *params,
                                              int flags) {
  struct mosquitto *mosq = (struct mosquitto *)params[0].aval;
  uint8_t *self = params[1].aval;
  void *slot = params[2].aval;
  const char *path = params[3].aval;
  uint16_t typeId = getTypeId(vm, getSlotType(vm, slot));
  uint16_t offset = getSlotHandle(vm, slot);

  void *type = getCompType(vm, self);
  const char *typeName = getTypeName(vm, type);

  // Assemble path
  MQTT_PATH_BUFFER[0] = 0;
  strncpy(MQTT_PATH_BUFFER, path, MAX_PATH_LENGTH);
  strncat(MQTT_PATH_BUFFER, "/", MAX_PATH_LENGTH);
  strncat(MQTT_PATH_BUFFER, getSlotName(vm, slot), MAX_PATH_LENGTH);
  // Compute the hash key
  MQTT_SLOT_KEY_TYPE key =
      MagolvesMqtt_CbcMiddlewareService_computeSlotKey(vm, self, slot);

  log_info("Register slot %p \n\t((k=0x%x), self=%p, qn=%s, n=%s, t=%d, off=%d "
           "(0x%x), p=%s -> %s",
           slot, (MQTT_SLOT_KEY_TYPE)key, self, typeName, getSlotName(vm, slot),
           typeId, offset, offset, path, MQTT_PATH_BUFFER);

  mqtt_add_slot_entry(mosq, (MQTT_SLOT_KEY_TYPE)key, self, offset, typeId,
                      MQTT_PATH_BUFFER);

  log_info("Added slot to map %p", slot);

  if ((flags & EXPORT_WRITABLE) > 0) {
    renderPayloadAsJson(JSON_BUFFER, self, offset, typeId);
    mosquitto_publish(mosq, NULL, MQTT_PATH_BUFFER, strlen(JSON_BUFFER),
                      JSON_BUFFER, 0, false);

    mosquitto_subscribe_v5(mosq, NULL, MQTT_PATH_BUFFER, 0 /*qos*/,
                           0 /* options*/, NULL);
  }
  return trueCell;
}

void changeListener(SedonaVM *vm, uint8_t *self, void *slot) {
  // log_info("Check slot to map s=%p (0x%x)", slot, (MQTT_SLOT_KEY_TYPE)slot);

  MQTT_SLOT_KEY_TYPE key =
      MagolvesMqtt_CbcMiddlewareService_computeSlotKey(vm, self, slot);

  const struct mqtt_slot_entry *se =
      mqtt_find_slot_entry((MQTT_SLOT_KEY_TYPE)key);
  if (se != NULL) {

    Cell args[2];
    args[0].aval = self;
    args[1].aval = slot;

    renderPayloadAsJson(JSON_BUFFER, self, se->slot, se->tid);
    /*
    log_info("Publish slot %s (%s, t=%d)\n", getSlotName(vm, slot), JSON_BUFFER,
             se->tid);*/
    mosquitto_publish(se->session, NULL, (const char *)se->path,
                      strlen(JSON_BUFFER), JSON_BUFFER, 0, false);
  }
}

void renderPayload(char *buffer, size_t *len, uint8_t *self, uint16_t offset,
                   uint16_t tid) {

  switch (tid) {
  case BoolTypeId:
  case ByteTypeId:
    *len = sizeof(uint8_t);
    uint8_t b = getByte(self, offset);
    memcpy(buffer, &b, *len);
    break;
  case ShortTypeId:
    *len = sizeof(uint16_t);
    uint16_t s = getShort(self, offset);
    memcpy(buffer, &s, *len);
    break;
  case IntTypeId:
  case FloatTypeId:
    *len = sizeof(int32_t);
    int32_t f = getInt(self, offset);
    memcpy(buffer, &f, *len);
    break;
  case DoubleTypeId:
    *len = sizeof(int64_t);
    int64_t l = getWide(self, offset);
    memcpy(buffer, &l, *len);
    break;
  default:
    log_warn("Invalid tid %d", tid);
    *len = 0;
    break;
  }
}

void renderPayloadAsJson(char *buffer, uint8_t *self, uint16_t offset,
                         uint16_t tid) {

  switch (tid) {
  case BoolTypeId:
    // this line is required via C99 std; otherwise we get a compile error
    // since every label mustfollowed by a statement and a
    // decl is NOT a statement (sigh)
    // We do not repeat ourselves, this is just language quirk
    buffer[0] = 0;
    uint8_t bl = getByte(self, offset);
    snprintf(buffer, MAX_JSON_LENGTH, "{\"V\": %s}",
             (bl > 0 ? "true" : "false"));
    break;
  case ByteTypeId:
    buffer[0] = 0;
    uint8_t b = getByte(self, offset);
    snprintf(buffer, MAX_JSON_LENGTH, "{\"V\": %d}", b);
    break;
  case ShortTypeId:
    buffer[0] = 0;
    uint16_t s = getShort(self, offset);
    snprintf(buffer, MAX_JSON_LENGTH, "{\"V\": %d}", s);
    break;
  case IntTypeId:
    buffer[0] = 0;
    int32_t i = getInt(self, offset);
    snprintf(buffer, MAX_JSON_LENGTH, "{\"V\": %d}", i);
    break;
  case FloatTypeId:
    buffer[0] = 0;
    float f = getFloat(self, offset);
    snprintf(buffer, MAX_JSON_LENGTH, "{\"V\": %.1f}", f);
    break;
  case DoubleTypeId:
    buffer[0] = 0;
    double d = (double)getWide(self, offset);
    snprintf(buffer, MAX_JSON_LENGTH, "{\"V\": %.1f}", d);
    break;
  case BufTypeId:
    buffer[0] = 0;
    const char *str = getInline(self, offset);
    sprintf(buffer, "{\"V\": \"%s\"}", str);
    break;
  default:
    buffer[0] = 0;
    sprintf(buffer, "{\"V\": \"(Invalid type id %d)\"}", tid);
    break;
  }
}

MQTT_SLOT_KEY_TYPE
MagolvesMqtt_CbcMiddlewareService_computeSlotKey(SedonaVM *vm, uint8_t *self,
                                                 void *slot) {
  uint16_t offset = getSlotHandle(vm, slot);
  return (self - vm->dataBaseAddr) + offset;
}
