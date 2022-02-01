#include <stdint.h>

#include "log.h"
#include "mqtt_map.h"

static struct mqtt_slot_entry *entries = NULL;

void mqtt_add_slot_entry(struct mosquitto *session, MQTT_SLOT_KEY_TYPE key,
                         uint8_t *self, uint8_t *sid, uint8_t tid,
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
  struct mqtt_slot_entry *entry;

  HASH_FIND_INT(entries, &key, entry); /* s: output pointer */
  return entry;
}

const struct mqtt_slot_entry *mqtt_find_path_entry(const char *path) {
  struct mqtt_slot_entry *entry, *tmp;

  HASH_ITER(hh, entries, entry, tmp) {
    // log_info("Check\n%s\n%s", path, entry->path);
    if (strcmp(path, entry->path) == 0) {
      return entry;
    }
  }

  return NULL;
}

int mqtt_count_component_slots(uint8_t *self) {
  struct mqtt_slot_entry *entry, *tmp;

  int result = 0;
  HASH_ITER(hh, entries, entry, tmp) {
    if (self == entry->self) {
      ++result;
    }
  }

  return result;
}

void mqtt_remove_all() {
  struct mqtt_slot_entry *entry, *tmp;
  HASH_ITER(hh, entries, entry, tmp) {
    HASH_DEL(entries, entry); /* delete; entries advances to next */
    free(entry);              /* optional- if you want to free  */
  }
}

int mqtt_map_size() { return HASH_COUNT(entries); }
