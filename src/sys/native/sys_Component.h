#ifndef SYS_COMPONENT_H
#define SYS_COMPONENT_H

#include <stdint.h>

#include "sedona.h"

typedef void (*set_callback)(SedonaVM *vm, uint8_t *comp, void *slot);

void sys_component_on_change(set_callback);

Cell sys_Component_doSetBool(SedonaVM *vm, Cell *params);
Cell sys_Component_doSetInt(SedonaVM *vm, Cell *params);
Cell sys_Component_doSetLong(SedonaVM *vm, Cell *params);
Cell sys_Component_doSetFloat(SedonaVM *vm, Cell *params);
Cell sys_Component_doSetDouble(SedonaVM *vm, Cell *params);

Cell sys_Component_invokeVoid(SedonaVM *vm, Cell *params);

#ifndef NULL
#define NULL 0
#endif

#endif