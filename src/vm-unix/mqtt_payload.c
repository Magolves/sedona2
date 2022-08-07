#include "mqtt_payload.h"
#include "constants.h"
#include "log.h"
#include "sedona.h"
#include <stdint.h>

#define TYPE_ID_SIZE sizeof(uint16_t)

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

bool render_is_valid_payload_length(size_t payload_len, uint8_t *self,
                                    uint16_t offset, uint16_t tid) {
  return render_get_length(self, offset, tid) + TYPE_ID_SIZE;
}

int render_payload_raw(uint8_t *buffer, size_t max_buf_len, uint8_t *self,
                       uint16_t offset, uint16_t tid) {

  size_t req_len = render_get_length(self, offset, tid);

  if (max_buf_len < req_len) {
    return -1;
  }

  switch (tid) {
  case BoolTypeId:
  case ByteTypeId:
    // this line is required via C99 std; otherwise we get a compile error
    // since every label mustfollowed by a statement and a
    // decl is NOT a statement (sigh)
    // We do not repeat ourselves, this is just language quirk
    memcpy(buffer, &tid, TYPE_ID_SIZE);
    buffer[TYPE_ID_SIZE] = getByte(self, offset);
    break;
  case ShortTypeId:
    memcpy(buffer, &tid, TYPE_ID_SIZE);
    uint16_t s = getShort(self, offset);
    memcpy(buffer + TYPE_ID_SIZE, &s, req_len);
    break;
  case IntTypeId:
    buffer[0] = tid;
    int32_t i = getInt(self, offset);
    memcpy(buffer + TYPE_ID_SIZE, &i, req_len);
    break;
  case FloatTypeId:
    memcpy(buffer, &tid, TYPE_ID_SIZE);
    float f = getFloat(self, offset);
    memcpy(buffer + TYPE_ID_SIZE, &f, req_len);
    break;
  case DoubleTypeId:
    memcpy(buffer, &tid, TYPE_ID_SIZE);
    double d = (double)getWide(self, offset);
    memcpy(buffer + TYPE_ID_SIZE, &d, req_len);
    break;
  case LongTypeId:
    memcpy(buffer, &tid, TYPE_ID_SIZE);
    long l = (long)getWide(self, offset);
    memcpy(buffer + TYPE_ID_SIZE, &l, req_len);
    break;
  case BufTypeId:
  default:
    // NOT SUPPORTED
    return -1;
  }

  return req_len;
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
