#ifndef MQTT_PAYLOAD
#define MQTT_PAYLOAD

#include "sedona.h"

void render_payload_raw(char *buffer, size_t *len, uint8_t *self,
                        uint16_t offset, uint16_t tid);

void render_payload_json(char *buffer, size_t max_buf_len, uint8_t *self,
                         uint16_t offset, uint16_t tid);

#endif /* MQTT_PAYLOAD */
