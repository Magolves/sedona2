#include "mqtt_callbacks.h"
#include "log.h"
#include "mqtt_map.h"
#include "sedona.h"
#include "sys_Component.h"

#include <mqtt_protocol.h>

#include <stdio.h>
#include <stdlib.h>

void mqtt_log_callback(struct mosquitto *mosq, void *obj, int level,
                       const char *str) {
  UNUSED(mosq);
  UNUSED(obj);
  UNUSED(level);

  printf("%s\n", str);
}

void mqtt_connect_callback_v5(struct mosquitto *mosq, void *obj, int result,
                              int flags, const mosquitto_property *properties) {
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
}

void mqtt_disconnect_callback(struct mosquitto *mosq, void *obj, int rc) {
  mqtt_disconnect_callback_v5(mosq, obj, rc, NULL);
}

void mqtt_subscribe_callback_v5(struct mosquitto *mosq, void *obj, int mid,
                                int qos_count, const int *granted_qos,
                                const mosquitto_property *props) {

  UNUSED(mosq);
  UNUSED(obj);
  UNUSED(mid);
  UNUSED(qos_count);
  UNUSED(granted_qos);
  UNUSED(props);
  log_info("Subscription ACK");
}

void mqtt_subscribe_callback(struct mosquitto *mosq, void *obj, int mid,
                             int qos_count, const int *granted_qos) {

  mqtt_subscribe_callback_v5(mosq, obj, mid, qos_count, granted_qos, NULL);
}

void mqtt_message_v5(struct mosquitto *mosq, void *obj,
                     const struct mosquitto_message *msg,
                     const mosquitto_property *props) {

  UNUSED(mosq);
  SedonaVM *vm = (SedonaVM *)obj;

  if (msg->retain) {
    return;
  }

  const struct mqtt_slot_entry *se = mqtt_find_path_entry(msg->topic);

  if (se != NULL) {

    Cell args[3];
    args[0].aval = se->self;
    args[1].aval = se->slot;

    // NOTE: We have to use sys_Component_... here in order to get informed
    // on value changes. Uset the set functions in sedona.h would
    // bypass the change handler

    log_info("Found: %s (%d) (l=%d) => %d", se->path, se->tid, msg->payloadlen,
             args[2].ival);

    if (se->action) {
      switch (se->tid) {
      case VoidTypeId:
        log_warn("Call void: %s", msg->topic);
        sys_Component_invokeVoid(vm, args);
        break;
      default:
        log_warn("Call: Invalid or unsupported tid: %d", se->tid);
      }
    } else {
      switch (se->tid) {
      case BoolTypeId:
        if (msg->payloadlen == 1) {
          args[2].ival = ((char *)msg->payload)[0] == '1' ||
                         ((char *)msg->payload)[0] == 't';
          sys_Component_doSetBool(vm, args);
        }
        break;
      default:
        log_warn("Set: Invalid or unsupported tid: %d", se->tid);
      }
    }
  } else {
    log_warn("Ignored: %s", msg->payload);
  }
}

void mqtt_message(struct mosquitto *mosq, void *obj,
                  const struct mosquitto_message *msg) {

  mqtt_message_v5(mosq, obj, msg, NULL);
}
