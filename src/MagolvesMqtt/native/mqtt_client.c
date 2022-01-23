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

static void changeListener(SedonaVM *vm, uint8_t *comp, void *slot);

static volatile int status = STATUS_CONNECTING;

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

Cell MagolvesMqtt_CbcMiddlewareService_exportSlot(SedonaVM *vm, Cell *params) {
  struct mosquitto *mosq = (struct mosquitto *)params[0].aval;
  uint8_t *self = params[1].aval;
  void *slot = params[2].aval;
  int16_t *paths = params[3].aval;
  uint16_t typeId = getTypeId(vm, getSlotType(vm, slot));
  uint16_t offset = getSlotHandle(vm, slot);

  void *type = getCompType(vm, self);
  const char *typeName = getTypeName(vm, type);

  log_info("Register slot %p (self=%p, qn=%s, n=%s, t=%d, cb=%p (off=0x%x, "
           "%ld), db=%p, "
           "(self-db)=0x%x (%d), offset=0x%x (%d))",
           slot, self, typeName, getSlotName(vm, slot), typeId,
           vm->codeBaseAddr, ((uint8_t *)slot) - vm->codeBaseAddr,
           ((uint8_t *)slot) - vm->codeBaseAddr, vm->dataBaseAddr,
           self - vm->dataBaseAddr, self - vm->dataBaseAddr, offset, offset);

  mqtt_add_slot_entry(offset, 0, typeId, paths);
  log_info("Added slot to map %p", slot);
}

void changeListener(SedonaVM *vm, uint8_t *comp, void *slot) {
  log_info("Check slot to map %p", slot);
  struct mqtt_slot_entry *se = mqtt_find_slot_entry((int)slot);
  if (se != NULL) {
    // log_info("Publish slot %s\n", getSlotName(vm, slot));
  }
}
