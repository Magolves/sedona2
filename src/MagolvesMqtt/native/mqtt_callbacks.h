#ifndef MQTT_CALLBACKS
#define MQTT_CALLBACKS

#include "constants.h"

#include <mosquitto.h>

void mqtt_log_callback(struct mosquitto *mosq, void *obj, int level,
                       const char *str);

void mqtt_connect_callback_v5(struct mosquitto *mosq, void *obj, int result,
                              int flags, const mosquitto_property *properties);

void mqtt_disconnect_callback_v5(struct mosquitto *mosq, void *obj, int rc,
                                 const mosquitto_property *properties);

void mqtt_connect_callback(struct mosquitto *mosq, void *obj, int result);
void mqtt_disconnect_callback(struct mosquitto *mosq, void *obj, int rc);

#endif /* MQTT_CALLBACKS */
