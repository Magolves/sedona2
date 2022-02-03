#include "mqtt_payload.h"
#include "constants.h"
#include "log.h"
#include "sedona.h"

size_t render_get_length(uint8_t *self, uint16_t offset, uint16_t tid) {

  switch (tid) {
  case BoolTypeId:
  case ByteTypeId:
    return sizeof(uint8_t);
  case ShortTypeId:
    return sizeof(uint16_t);
  case IntTypeId:
  case FloatTypeId:
    return sizeof(int32_t);
  case DoubleTypeId:
  case LongTypeId:
    return sizeof(int64_t);
  default:
    // how to determine length of buffer?
    // const char *str = getInline(self, offset);
    return 0;
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
    snprintf(buffer, max_buf_len, "{\"V\": \"%s\"}", str);
    break;
  default:
    buffer[0] = 0;
    sprintf(buffer, "{\"V\": \"(Invalid type id %d)\"}", tid);
    break;
  }
}
