#ifndef MQTT_CLIENT
#define MQTT_CLIENT

#include "sedona.h"

///////////////////////////////////////////////////////
// Native Method Slots
///////////////////////////////////////////////////////
Cell MagolvesMqtt_CbcMiddlewareService_startSession(SedonaVM *vm, Cell *params);
Cell MagolvesMqtt_CbcMiddlewareService_stopSession(SedonaVM *vm, Cell *params);
Cell MagolvesMqtt_CbcMiddlewareService_isSessionLive(SedonaVM *vm,
                                                     Cell *params);
Cell MagolvesMqtt_CbcMiddlewareService_getStatus(SedonaVM *vm, Cell *params);

Cell MagolvesMqtt_CbcMiddlewareService_exportMonitor(SedonaVM *vm,
                                                     Cell *params);
Cell MagolvesMqtt_CbcMiddlewareService_exportParameter(SedonaVM *vm,
                                                       Cell *params);
Cell MagolvesMqtt_CbcMiddlewareService_exportAction(SedonaVM *vm, Cell *params);

Cell MagolvesMqtt_CbcMiddlewareService_export(SedonaVM *vm, Cell *params,
                                              int flags);

Cell MagolvesMqtt_CbcMiddlewareService_execute(SedonaVM *vm, Cell *params);

#endif /* MQTT_CLIENT */
