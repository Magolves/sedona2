#ifndef MQTT_CLIENT
#define MQTT_CLIENT

#include "sedona.h"

///////////////////////////////////////////////////////
// Native Method Slots
///////////////////////////////////////////////////////
Cell cbcmw_CbcMiddlewareService_startSession(SedonaVM *vm, Cell *params);
Cell cbcmw_CbcMiddlewareService_stopSession(SedonaVM *vm, Cell *params);
Cell cbcmw_CbcMiddlewareService_isSessionLive(SedonaVM *vm, Cell *params);

#endif /* MQTT_CLIENT */
