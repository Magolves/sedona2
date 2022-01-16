#include "mqtt_client.h"

#include <stdio.h>

int main(int argc, const char *argv[]) {
  Cell arg;
  Cell val = cbcmw_CbcMiddlewareService_startSession(NULL, &arg);


  Cell result = cbcmw_CbcMiddlewareService_stopSession(NULL, &val);
  return 0;
}