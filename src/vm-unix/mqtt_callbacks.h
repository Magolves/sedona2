#ifndef MQTT_CALLBACKS
#define MQTT_CALLBACKS

#include "constants.h"

#include <mosquitto.h>

/// @brief See
/// https://mosquitto.org/api/files/mosquitto-h.html#mosquitto_log_callback_set
void mqtt_log_callback(struct mosquitto *mosq, void *obj, int level,
                       const char *str);

/// @brief See
/// https://mosquitto.org/api/files/mosquitto-h.html#mosquitto_connect_callback_set
void mqtt_connect_callback_v5(struct mosquitto *mosq, void *obj, int result,
                              int flags, const mosquitto_property *properties);
void mqtt_connect_callback(struct mosquitto *mosq, void *obj, int result);

/// @brief See
/// https://mosquitto.org/api/files/mosquitto-h.html#mosquitto_disconnect_callback_set
void mqtt_disconnect_callback_v5(struct mosquitto *mosq, void *obj, int rc,
                                 const mosquitto_property *properties);

void mqtt_disconnect_callback(struct mosquitto *mosq, void *obj, int rc);

void mqtt_subscribe_callback_v5(struct mosquitto *mosq, void *obj, int mid,
                                int qos_count, const int *granted_qos,
                                const mosquitto_property *props);
void mqtt_subscribe_callback(struct mosquitto *mosq, void *obj, int mid,
                             int qos_count, const int *granted_qos);

/// @brief Callbacks when a message has been received by the broker
/// https://mosquitto.org/api/files/mosquitto-h.html#mosquitto_message_callback_set
void mqtt_message_v5(struct mosquitto *mosq, void *obj,
                     const struct mosquitto_message *msg,
                     const mosquitto_property *props);
void mqtt_message(struct mosquitto *mosq, void *obj,
                  const struct mosquitto_message *msg);

#endif /* MQTT_CALLBACKS */
