#ifndef MQTT_PAYLOAD
#define MQTT_PAYLOAD

#include "sedona.h"
#include <stdint.h>

/// @brief Determines the payload size according to the type
///
/// @param self the component pointer
/// @param offset the slot handle/offset
/// @param tid the Sedona type ID
/// @return int the required amount of bytes to store the value
size_t render_get_length(uint8_t *self, uint16_t offset, uint16_t tid);

/// @brief Check if given payload has the proper length for a payload in
/// our proprietary format.
///
/// @param payload_len the payload length of the (received) frame)
/// @param self the component pointer
/// @param offset the slot handle/offset
/// @param tid the Sedona type ID
/// @return true payload has valid length
/// @return false payload is either too short or too long for this type
bool render_is_valid_payload_length(size_t payload_len, uint8_t *self,
                                    uint16_t offset, uint16_t tid);

/// @brief Renders the given value into a proprietary raw format. The first word
/// (HSB, LSB) is the Sedona type id and subsequent bytes are the raw bytes. The byte ordering
/// depends on the machine.
/// @param buffer the receiving byte buffer
/// @param max_buf_len the maximum capacity of the buffer.
/// @param self the component pointer
/// @param offset the slot handle/offset
/// @param tid the Sedona type ID
/// @return int the number of copied bytes
int render_payload_raw(uint8_t *buffer, size_t max_buf_len, uint8_t *self,
                       uint16_t offset, uint16_t tid);

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
