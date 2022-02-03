#include "mqtt_payload.h"
#include "constants.h"
#include "log.h"

void render_payload_raw(char *buffer, size_t *len, uint8_t *self,
                        uint16_t offset, uint16_t tid) {

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

void render_payload_json(char *buffer, size_t max_buf_len, uint8_t *self,
                         uint16_t offset, uint16_t tid) {

  switch (tid) {
  case BoolTypeId:
    // this line is required via C99 std; otherwise we get a compile error
    // since every label mustfollowed by a statement and a
    // decl is NOT a statement (sigh)
    // We do not repeat ourselves, this is just language quirk
    buffer[0] = 0;
    uint8_t bl = getByte(self, offset);
    snprintf(buffer, max_buf_len, "{\"V\": %s}", (bl > 0 ? "true" : "false"));
    break;
  case ByteTypeId:
    buffer[0] = 0;
    uint8_t b = getByte(self, offset);
    snprintf(buffer, max_buf_len, "{\"V\": %d}", b);
    break;
  case ShortTypeId:
    buffer[0] = 0;
    uint16_t s = getShort(self, offset);
    snprintf(buffer, max_buf_len, "{\"V\": %d}", s);
    break;
  case IntTypeId:
    buffer[0] = 0;
    int32_t i = getInt(self, offset);
    snprintf(buffer, max_buf_len, "{\"V\": %d}", i);
    break;
  case FloatTypeId:
    buffer[0] = 0;
    float f = getFloat(self, offset);
    snprintf(buffer, max_buf_len, "{\"V\": %.1f}", f);
    break;
  case DoubleTypeId:
    buffer[0] = 0;
    double d = (double)getWide(self, offset);
    snprintf(buffer, max_buf_len, "{\"V\": %.1f}", d);
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
