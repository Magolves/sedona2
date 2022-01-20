#include <stdint.h>

#include "MQTTMap.h"

static struct mqtt_slot_entry *entries = NULL;

void mqtt_add_slot_entry(void *session, int slot, uint8_t tid, int16_t *path)
{
    struct mqtt_slot_entry *s;

    HASH_FIND_INT(entries, &slot, s);  /* slot already in the hash? */
    if (s == NULL) {
        s = (struct mqtt_slot_entry*)malloc(sizeof(struct mqtt_slot_entry));
        s->slot = slot;
        s->session = session;
        s->lastSent = 0L;
        memcpy(s->path, path, sizeof(int16_t) * MAX_PATH_LEN);

        HASH_ADD_INT( entries, slot, s );  /* slot: name of key field */
    }    
}

struct mqtt_slot_entry *mqtt_find_slot_entry(int slot)
{
    struct mqtt_slot_entry *s;

    HASH_FIND_INT( entries, &slot, s );  /* s: output pointer */
    return s;
}


