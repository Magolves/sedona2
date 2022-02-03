
#ifndef MQTT_MAP
#define MQTT_MAP

#include "uthash.h"
#include <mosquitto.h>
#include <stdint.h>

#define MAX_PATH_LEN 256

#define MQTT_SLOT_KEY_TYPE int

struct mqtt_slot_entry {
  MQTT_SLOT_KEY_TYPE key; /* hash key */
  /// @brief Holds the mosquitto session. TODO: Check if this is really
  /// required, since it is a waste of memory.
  struct mosquitto *session;
  uint8_t tid;
  uint8_t *self;
  uint8_t *slot;
  const char path[MAX_PATH_LEN];
  uint8_t action : 1;
  uint8_t changed : 1;
  uint8_t unused : 6;

  UT_hash_handle hh; /* makes this structure hashable */
};

/// @brief Add a slot (entry) to the internal hash map.
///
/// @param session the MQTT session pointer
/// @param key the slot handle/pointer
/// @param self the self pointer (component)
/// @param sid the slot id
/// @param tid the slot type id
/// @param path the MQTT topic
void mqtt_add_slot_entry(struct mosquitto *session, MQTT_SLOT_KEY_TYPE key,
                         uint8_t *self, uint8_t *sid, uint8_t tid,
                         const char *path);

/// @brief Add an action slot (entry) to the internal hash map.
///
/// @param session the MQTT session pointer
/// @param key the action slot handle/pointer
/// @param self the self pointer (component)
/// @param sid the slot id
/// @param tid the action parameter type id (0 = void)
/// @param path the MQTT topic
void mqtt_add_action_entry(struct mosquitto *session, MQTT_SLOT_KEY_TYPE key,
                           uint8_t *self, uint8_t *sid, uint8_t tid,
                           const char *path);

/// @brief Finds an entry matching the given key.
///
/// @param path the key to match
/// @return const struct mqtt_slot_entry* the matching map entry or NULL
const struct mqtt_slot_entry *mqtt_find_slot_entry(MQTT_SLOT_KEY_TYPE key);

/// @brief Finds an entry matching the given path.
///
/// @param path the path to match
/// @return const struct mqtt_slot_entry* the matching map entry or NULL
const struct mqtt_slot_entry *mqtt_find_path_entry(const char *path);

/// @brief Removes the entry with the given key
///
/// @param path the key to match
/// @return const struct mqtt_slot_entry* the matching map entry or NULL
const struct mqtt_slot_entry *mqtt_find_slot_entry(MQTT_SLOT_KEY_TYPE key);

/// @brief Count the registered slots for the given component.
///
/// @param self the component pointer
/// @return int the number of slots registered for this component
int mqtt_count_component_slots(uint8_t *self);

/// @brief Remove all entries from the map.
void mqtt_remove_all();

/// @brief Get the  number of map entries
///
/// @return int
int mqtt_map_size();

#endif /* MQTT_MAP */
