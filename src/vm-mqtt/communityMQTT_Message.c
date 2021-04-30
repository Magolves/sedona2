#ifdef __DARWIN__
#include <sys/types.h>
#endif

#include <stdio.h>
#include "MQTTClient.h"
#include "MQTTMap.h"
#include "communityMQTT_common.h"
#include "sys_Component.h"

#include <stdio.h>
#include <signal.h>
#include <memory.h>

#include <sys/time.h>

#include "sedona.h"
#include "uthash.h"
#include "log.h"

void changeListener(SedonaVM* vm, uint8_t *comp, void *slot);

// native method slots
Cell communityMQTT_Message_doPublish(SedonaVM* vm, Cell* params)
{
  SessionHandle * pSession = (SessionHandle *)params[0].aval;
  char * topic = params[1].aval;
  char * payload = params[2].aval;
  int32_t payload_len = params[3].ival;
  int32_t qos = params[4].ival;
  
  Payload * pPayload = malloc(sizeof(Payload));
  pPayload->type = PublishTask;

  PublishData * pData = malloc(sizeof(PublishData));
  pData->topic = malloc(strlen(topic)+1);
  strcpy(pData->topic, topic);
  pData->payload = malloc(payload_len);
  memcpy(pData->payload, payload, payload_len);
  pData->payload_len = payload_len;
  pData->qos = qos;
  pPayload->pPublishData = pData;

  pushPayload(pSession, pPayload);
  return nullCell;
}

Cell communityMQTT_Message_doSubscribe(SedonaVM* vm, Cell* params)
{
  SessionHandle * pSession = (SessionHandle *)params[0].aval;
  char * topic = params[1].aval;
  int32_t qos = params[2].ival;
  
  Payload * pPayload = malloc(sizeof(Payload));
  pPayload->type = SubscribeTask;
  SubscribeData * pData = malloc(sizeof(SubscribeData));
  pData->topic = malloc(strlen(topic)+1);
  strcpy(pData->topic, topic);
  pData->qos = qos;
  pPayload->pSubscribeData = pData;

  pushPayload(pSession, pPayload);
  return nullCell;
}

Cell communityMQTT_Message_doUnsubscribe(SedonaVM* vm, Cell* params)
{
  SessionHandle * pSession = (SessionHandle *)params[0].aval;
  char * topic = params[1].aval;
  
  Payload * pPayload = malloc(sizeof(Payload));
  pPayload->type = UnsubscribeTask;
  UnsubscribeData * pData = malloc(sizeof(UnsubscribeData));
  pData->topic = malloc(strlen(topic)+1);
  strcpy(pData->topic, topic);
  pPayload->pUnsubscribeData = pData;

  pushPayload(pSession, pPayload);
  return nullCell;
}

Cell communityMQTT_Message_fetchData(SedonaVM* vm, Cell* params)
{
  SessionHandle * pSession = (SessionHandle *)params[0].aval;
  char * topic = params[1].aval;
  char * buf = params[2].aval;
  int32_t length = params[3].ival;
  
  if (!pSession || !pSession->pResponse)
    return falseCell;

  SubscribeResponse * pResponse = NULL;
  HASH_FIND_STR(pSession->pResponse, topic, pResponse);
  if (!pResponse)
    return falseCell;

  bool changed = false;
  int32_t strLen = strlen(buf);
  if (strLen >= length)
      strLen = length - 1;
  
  if (pResponse->payload_len < strLen || (pResponse->payload_len > strLen && pResponse->payload_len < length)) 
    changed = true;
  else if (pResponse->payload_len == strLen)
    changed = memcmp(pResponse->payload, buf, strLen) != 0;
  else // pResponse->payload_len >= length
    changed = memcmp(pResponse->payload, buf, length-1) != 0;

  //only return when data changed
  if (!changed)
    return falseCell;

  int32_t minLen = pResponse->payload_len > length-1 ? length-1 : pResponse->payload_len;
  if (minLen < 0)
    return falseCell;

  strncpy(buf, pResponse->payload, minLen);
  buf[minLen] = 0;
  return trueCell;
}

Cell communityMQTT_Message_regSlot(SedonaVM* vm, Cell* params)
{
  SessionHandle * pSession = (SessionHandle *)params[0].aval;
  uint8_t* self   = params[1].aval;
  void* slot      = params[2].aval;
  int16_t * paths = params[3].aval;
  uint16_t typeId = getTypeId(vm, getSlotType(vm, slot));
  uint16_t offset = getSlotHandle(vm, slot);

  void* type = getCompType(vm, self);
  const char* typeName = getTypeName(vm, type);

  log_info("Register slot %p (self=%p, qn=%s, n=%s, t=%d)", 
    slot, 
    self, 
    typeName, 
    getSlotName(vm, slot),
    typeId);

  sys_component_on_change(changeListener);

  mqtt_add_slot_entry(pSession, (int)slot, typeId, paths);
  log_info("Added slot to map %p", slot);
}

void changeListener(SedonaVM* vm, uint8_t *comp, void *slot)
{
  log_info("Check slot to map %p", slot);
  struct mqtt_slot_entry *se = mqtt_find_slot_entry((int)slot);
  if (se != NULL) {
    log_info("Publish slot %s\n",getSlotName(vm, slot));
  }
}
