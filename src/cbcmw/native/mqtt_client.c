#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#ifndef WIN32
#include <sys/time.h>
#include <time.h>
#else
#include <process.h>
#include <winsock2.h>
#define snprintf sprintf_s
#endif

#include <mqtt_protocol.h>
#include <mosquitto.h>

#include "sedona.h"

void mqtt_log_callback(struct mosquitto *mosq, void *obj, int level, const char *str)
{
	UNUSED(mosq);
	UNUSED(obj);
	UNUSED(level);

	printf("%s\n", str);
}

///////////////////////////////////////////////////////
// Native Method Slots
///////////////////////////////////////////////////////
Cell communityMQTT_Worker_startSession(SedonaVM* vm, Cell* params)
{
  char * host = params[0].aval;
  int32_t port = params[1].ival;
  char * clientid = params[2].aval;
  char * username = params[3].aval;
  char * password = params[4].aval;
  
  struct mosquitto *mosq = NULL;
	int rc;

  mosquitto_lib_init();

  mosq = mosquitto_new(MW_CLIENT_NAME, MW_CLEAN_SESSION, NULL);
	if(!mosq){
		switch(errno){
			case ENOMEM:
				err_printf(&cfg, "Error: Out of memory.\n");
				break;
			case EINVAL:
				err_printf(&cfg, "Error: Invalid id.\n");
				break;
		}
		goto cleanup;
	}
	
	mosquitto_log_callback_set(mosq, mqtt_log_callback);
	


  Cell result;
  
  return result;
}

Cell communityMQTT_Worker_stopSession(SedonaVM* vm, Cell* params)
{

  return nullCell;
}

Cell communityMQTT_Worker_isSessionLive(SedonaVM* vm, Cell* params)
{
  SessionHandle * pSession = (SessionHandle *)params[0].aval;
  if (!pSession)
    return falseCell;

  MQTTHandle * pHandle = pSession->pHandle;
  if (pHandle && pHandle->pClient && pHandle->pClient->isconnected)
    return trueCell;
  else
    return falseCell;
}