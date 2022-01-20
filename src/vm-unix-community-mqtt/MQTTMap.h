
#ifndef MQTTMAP_H
#define MQTTMAP_H

#include "uthash.h"

#define MAX_PATH_LEN 5

struct mqtt_slot_entry {
  uint8_t tid; /* key */
  int slot;
  void *session;
  long lastSent;
  int16_t path[MAX_PATH_LEN];
  UT_hash_handle hh; /* makes this structure hashable */
};

void mqtt_add_slot_entry(void *session, int slot, uint8_t tid, int16_t *path);
struct mqtt_slot_entry *mqtt_find_slot_entry(int slot);

#endif /* MQTTMAP_H */
