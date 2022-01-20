#include "mqtt_client.h"
#include "sedona.h"

#include <stdio.h>

char *HOST = "localhost";

int main(int argc, const char *argv[]) {
  Cell arg[5];
  arg[0].aval = HOST;
  arg[1].ival = 1883;
  arg[2].aval = NULL;
  arg[3].aval = NULL;
  arg[4].aval = NULL;

  Cell mosq = cbcmw_CbcMiddlewareService_startSession(NULL, arg);

  if (mosq.aval == NULL) {
    printf("Error: Init failed");
    return 1;
  }

  for (int i = 0; i < 100000; i++) {
    cbcmw_CbcMiddlewareService_execute(NULL, &mosq);
  }

  Cell result = cbcmw_CbcMiddlewareService_stopSession(NULL, &mosq);
  return 0;
}