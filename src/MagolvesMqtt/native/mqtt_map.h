
#ifndef MQTT_MAP
#define MQTT_MAP

#include "uthash.h"

#define MAX_PATH_LEN 5

struct mqtt_slot_entry {
  uint16_t offset;
  uint8_t tid; /* key */
  uint8_t slot;
  int16_t path[MAX_PATH_LEN];
  UT_hash_handle hh; /* makes this structure hashable */
};

/// @brief Add a slot (entry) to the internal hash map.
///
/// @param tid the type id
/// @param sid the slot number within the Sedona component
/// @param path
void mqtt_add_slot_entry(uint16_t offset, uint8_t tid, uint8_t sid,
                         int16_t *path);

struct mqtt_slot_entry *mqtt_find_slot_entry(uint16_t offset);

#endif /* MQTT_MAP */
