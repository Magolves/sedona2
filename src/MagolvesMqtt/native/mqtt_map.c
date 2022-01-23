#include <stdint.h>

#include "mqtt_map.h"

static struct mqtt_slot_entry *entries = NULL;

void mqtt_add_slot_entry(MQTT_SLOT_KEY_TYPE offset, uint8_t tid, uint8_t sid,
                         int16_t *path) {
  struct mqtt_slot_entry *s;

  HASH_FIND_INT(entries, &offset, s); /* slot already in the hash? */
  if (s == NULL) {
    s = (struct mqtt_slot_entry *)malloc(sizeof(struct mqtt_slot_entry));
    s->offset = offset;
    s->slot = sid;
    s->tid = tid;
    memcpy(s->path, path, sizeof(int16_t) * MAX_PATH_LEN);

    HASH_ADD_INT(entries, offset, s); /* slot: name of key field */
  }
}

struct mqtt_slot_entry *mqtt_find_slot_entry(MQTT_SLOT_KEY_TYPE offset) {
  struct mqtt_slot_entry *s;

  HASH_FIND_INT(entries, &offset, s); /* s: output pointer */
  return s;
}
