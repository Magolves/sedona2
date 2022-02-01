#include <stdint.h>

#include "log.h"
#include "mqtt_map.h"

static struct mqtt_slot_entry *entries = NULL;

void mqtt_add_slot_entry(struct mosquitto *session, MQTT_SLOT_KEY_TYPE key,
                         uint8_t *self, uint8_t sid, uint8_t tid,
                         const char *path) {

  struct mqtt_slot_entry *s;

  HASH_FIND_INT(entries, &key, s); /* slot already in the hash? */
  if (s == NULL) {
    s = (struct mqtt_slot_entry *)malloc(sizeof(struct mqtt_slot_entry));
    s->session = session;
    s->key = key;
    s->slot = sid;
    s->self = self;
    s->tid = tid;

    log_info("Added slot entry %s (%d)", path, strlen(path));
    // s->path = malloc((strlen(path) + 1) * sizeof(char));
    strncpy((char *)s->path, (const char *)path, MAX_PATH_LEN);

    HASH_ADD_INT(entries, key, s); /* slot: name of key field */
  }
}

const struct mqtt_slot_entry *mqtt_find_slot_entry(MQTT_SLOT_KEY_TYPE key) {
  struct mqtt_slot_entry *s;

  HASH_FIND_INT(entries, &key, s); /* s: output pointer */
  return s;
}

const struct mqtt_slot_entry *mqtt_find_path_entry(const char *path) {
  struct mqtt_slot_entry *se, *tmp;
  HASH_ITER(hh, entries, se, tmp) {
    log_info("Check\n%s\n%s", path, se->path);
    if (strcmp(path, se->path) == 0) {
      return se;
    }
  }

  return NULL;
}

int mqtt_map_size() { return HASH_COUNT(entries); }
