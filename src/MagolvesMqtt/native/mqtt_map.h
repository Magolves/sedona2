
#ifndef MQTT_MAP
#define MQTT_MAP

#include "uthash.h"
#include <mosquitto.h>

#define MAX_PATH_LEN 256

#define MQTT_SLOT_KEY_TYPE int

struct mqtt_slot_entry {
  MQTT_SLOT_KEY_TYPE offset;
  /// @brief Holds the mosquitto session. TODO: Check if this is really
  /// required, since it is a waste of memory.
  struct mosquitto *session;
  uint8_t tid; /* key */
  uint8_t slot;
  const char path[MAX_PATH_LEN];
  UT_hash_handle hh; /* makes this structure hashable */
};

/// @brief Add a slot (entry) to the internal hash map.
///
/// @param session the MQTT session pointer
/// @param offset the slot handle/pointer
/// @param tid the slot type id
/// @param sid the slot id
/// @param path the MQTT topic
void mqtt_add_slot_entry(struct mosquitto *session, MQTT_SLOT_KEY_TYPE offset,
                         uint8_t tid, uint8_t sid, const char *path);

const struct mqtt_slot_entry *mqtt_find_slot_entry(MQTT_SLOT_KEY_TYPE offset);

const struct mqtt_slot_entry *mqtt_find_path_entry(const char *path);

#endif /* MQTT_MAP */
