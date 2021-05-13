#ifndef SYS_COMPONENT_H
#define SYS_COMPONENT_H

#include <stdint.h>

#include "sedona.h"

typedef void (*set_callback)(SedonaVM *vm, uint8_t *comp, void *slot);

void sys_component_on_change(set_callback);

#ifndef NULL
#define NULL 0
#endif

#endif