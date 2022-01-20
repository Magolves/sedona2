#include "mqtt_callbacks.h"

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
