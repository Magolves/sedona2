#ifndef MQTT_PAYLOAD
#define MQTT_PAYLOAD

#include "sedona.h"

/// @brief Determines the payload size according to the type
///
/// @param self the component pointer
/// @param offset the slot handle/offset
/// @param tid the Sedona type ID
/// @return int the required amount of bytes to store the value
size_t render_get_length(uint8_t *self, uint16_t offset, uint16_t tid);

/// @brief Renders the given slot value as JSON string. The JSON data is written
/// into the buffer.
///
/// @param buffer the pointer to the (JSON) buffer. The content will be
/// overwritten.
/// @param max_buf_len the maximum buffer length. If the buffer is too small,
/// the JSON string will be truncated!
/// @param self the component pointer
/// @param offset the slot handle/offset
/// @param tid the Sedona type ID
void render_payload_json(char *buffer, size_t max_buf_len, uint8_t *self,
                         uint16_t offset, uint16_t tid);

#endif /* MQTT_PAYLOAD */
