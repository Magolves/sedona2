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

#include <mosquitto.h>
#include <mqtt_protocol.h>

#include "constants.h"
#include "mqtt_client.h"

static volatile int status = STATUS_CONNECTING;
static int connack_result = 0;

void mqtt_log_callback(struct mosquitto *mosq, void *obj, int level,
                       const char *str) {
  UNUSED(mosq);
  UNUSED(obj);
  UNUSED(level);

  printf("%s\n", str);
}

void mqtt_connect_callback_v5(struct mosquitto *mosq, void *obj, int result,
                              int flags, const mosquitto_property *properties) {
  int rc = MOSQ_ERR_SUCCESS;

  UNUSED(obj);
  UNUSED(flags);
  UNUSED(properties);

  // connack_result = result;

  if (result) {
    mosquitto_disconnect_v5(mosq, 0, /*cfg.disconnect_props*/ NULL);
  }
}

void mqtt_connect_callback(struct mosquitto *mosq, void *obj, int result) {
  mqtt_connect_callback_v5(mosq, obj, result, 0, NULL);
}

void mqtt_disconnect_callback_v5(struct mosquitto *mosq, void *obj, int rc,
                                 const mosquitto_property *properties) {
  UNUSED(mosq);
  UNUSED(obj);
  UNUSED(rc);
  UNUSED(properties);

  if (rc == 0) {
    status = STATUS_DISCONNECTED;
  }
}

void mqtt_disconnect_callback(struct mosquitto *mosq, void *obj, int rc) {
  mqtt_disconnect_callback_v5(mosq, obj, rc, NULL);
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
Cell cbcmw_CbcMiddlewareService_startSession(SedonaVM *vm, Cell *params) {
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
    status = STATUS_INTERNAL_ERROR;
  } else {
    // Set MQTT protocol version to 5
    mosquitto_int_option(mosq, MOSQ_OPT_PROTOCOL_VERSION, MQTT_PROTOCOL_V5);
    // Specify example last will (must be done before connect)
    mosquitto_will_set_v5(mosq, "last/Will", 0, NULL, 0, false, NULL);

    mosquitto_log_callback_set(mosq, mqtt_log_callback);
    mosquitto_connect_v5_callback_set(mosq, mqtt_connect_callback_v5);
    mosquitto_disconnect_v5_callback_set(mosq, mqtt_disconnect_callback_v5);

    mosquitto_loop_start(mosq);
    if (rc != MOSQ_ERR_SUCCESS) {
      if (rc == MOSQ_ERR_INVAL) {
        printf("mosquitto_loop_start: Invalid parameters %s (%d)\n", host,
               port);
      } else {
        printf("mosquitto_loop_start: Illegal call (rc = %d)\n", rc);
      }
    } else {
      status = STATUS_CONNECTING;
      rc = mosquitto_connect_async(mosq, host, port, 60);
      if (rc != MOSQ_ERR_SUCCESS) {
        if (rc == MOSQ_ERR_INVAL) {
          printf("mosquitto_connect_async: Invalid parameters %s (%d)\n", host,
                 port);
        } else {
          printf("mosquitto_connect_async: Illegal call (rc = %d)\n", rc);
        }
      } else {
        status = STATUS_WAITING;
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
Cell cbcmw_CbcMiddlewareService_stopSession(SedonaVM *vm, Cell *params) {
  struct mosquitto *mosq = (struct mosquitto *)params[0].aval;
  if (!mosq)
    return falseCell;

  mosquitto_disconnect_v5(mosq, MQTT_RC_DISCONNECT_WITH_WILL_MSG, NULL);

  return trueCell;
}

/// @brief Checks if MQTT session is alive.
///
/// @param vm the VM instance
/// @param params the paramater array
/// @return Cell trueCell, if session is alive/connected
Cell cbcmw_CbcMiddlewareService_isSessionLive(SedonaVM *vm, Cell *params) {
  struct mosquitto *mosq = (struct mosquitto *)params[0].aval;
  if (!mosq)
    return falseCell;

  return (status == STATUS_WAITING) ? trueCell : falseCell;
}

/// @brief Executes a single MQTT loop. Can only be used if mosquitto is
/// operated synchronously.
///
/// @param vm the VM instance
/// @param params the paramater array
/// @return Cell the result
Cell cbcmw_CbcMiddlewareService_execute(SedonaVM *vm, Cell *params) {
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