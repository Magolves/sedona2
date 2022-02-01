#ifndef MQTT_CLIENT
#define MQTT_CLIENT

#include "sedona.h"

///////////////////////////////////////////////////////
// Native Method Slots
///////////////////////////////////////////////////////
Cell MagolvesMqtt_MiddlewareService_startSession(SedonaVM *vm, Cell *params);
Cell MagolvesMqtt_MiddlewareService_stopSession(SedonaVM *vm, Cell *params);
Cell MagolvesMqtt_MiddlewareService_isSessionLive(SedonaVM *vm, Cell *params);
Cell MagolvesMqtt_MiddlewareService_getStatus(SedonaVM *vm, Cell *params);

Cell MagolvesMqtt_MiddlewareService_registerReadOnlySlot(SedonaVM *vm,
                                                         Cell *params);
Cell MagolvesMqtt_MiddlewareService_registerWritableSlot(SedonaVM *vm,
                                                         Cell *params);
Cell MagolvesMqtt_MiddlewareService_registerAction(SedonaVM *vm, Cell *params);

Cell MagolvesMqtt_MiddlewareService_export(SedonaVM *vm, Cell *params,
                                           int flags);

Cell MagolvesMqtt_MiddlewareService_isComponentRegistered(SedonaVM *vm,
                                                          Cell *params);
Cell MagolvesMqtt_MiddlewareService_isSlotRegistered(SedonaVM *vm,
                                                     Cell *params);
Cell MagolvesMqtt_MiddlewareService_unregisterSlot(SedonaVM *vm, Cell *params);
Cell MagolvesMqtt_MiddlewareService_unregisterAllSlots(SedonaVM *vm,
                                                       Cell *params);

Cell MagolvesMqtt_MiddlewareService_enableComponentIf(SedonaVM *vm,
                                                      Cell *params);
Cell MagolvesMqtt_MiddlewareService_enableSlotIf(SedonaVM *vm, Cell *params);
Cell MagolvesMqtt_MiddlewareService_isSlotEnabled(SedonaVM *vm, Cell *params);

Cell MagolvesMqtt_MiddlewareService_getRegisteredSlotCount(SedonaVM *vm,
                                                           Cell *params);

// Internal methods (not exposed to Sedona)
Cell MagolvesMqtt_MiddlewareService_registerSlot(SedonaVM *vm, Cell *params);
Cell MagolvesMqtt_MiddlewareService_execute(SedonaVM *vm, Cell *params);

#endif /* MQTT_CLIENT */
