
```bash
$sedonac kits.xml
...
NOTE: Offset is the 
--- mqtteval::MqttComponent [sizeof 52, offset 6384] ---
    36:  mqtteval::MqttComponent.isHeating
    37:  mqtteval::MqttComponent.out
    38:  mqtteval::MqttComponent.raise
    39:  mqtteval::MqttComponent.lower
    40:  mqtteval::MqttComponent.diff
    44:  mqtteval::MqttComponent.sp
    48:  mqtteval::MqttComponent.cv
```

```text
[Generate] Slot mqtteval::MqttComponent.diff:40|8550
[Generate] Slot mqtteval::MqttComponent.isHeating:36|8558
[Generate] Slot mqtteval::MqttComponent.sp:44|8566
[Generate] Slot mqtteval::MqttComponent.cv:48|8574
[Generate] Slot mqtteval::MqttComponent.out:37|8582
[Generate] Slot mqtteval::MqttComponent.raise:38|8590
[Generate] Slot mqtteval::MqttComponent.lower:39|8598
```

```bash
$cd build/src/vm-mqtt
$./vmmq ../../../scode/test-mqtt.scode ../../../apps/test-mqtt.sab
```

yields

```text
Sedona VM 2.0.0
buildDate: Jan 18 2022 20:02:13
endian:    little
blockSize: 4
refSize:   4

-- MESSAGE [sys::App] starting
-- MESSAGE [sox::SoxService] started port=1876
-- MESSAGE [sox::SoxService] DASP Discovery enabled
-- MESSAGE [communityMQTT::MQTTService] MQTT service started
22:50:51 INFO  /home/oliver/dev/sedona2/src/vm-mqtt/communityMQTT_Message.c:137: Register slot 0xf7f3f188 (self=0x95e3e00, qn=MqttComponent, n=cv, t=6, cb=0xf7f3d010 (off=0x2178, 8568), db=0x95df700, (self-db)=0x4700 (18176), offset=0x30 (48))
22:50:51 INFO  /home/oliver/dev/sedona2/src/vm-mqtt/communityMQTT_Message.c:142: Added slot to map 0xf7f3f188
22:50:51 INFO  /home/oliver/dev/sedona2/src/vm-mqtt/communityMQTT_Message.c:137: Register slot 0xf7f3f180 (self=0x95e3e00, qn=MqttComponent, n=sp, t=6, cb=0xf7f3d010 (off=0x2170, 8560), db=0x95df700, (self-db)=0x4700 (18176), offset=0x2c (44))
22:50:51 INFO  /home/oliver/dev/sedona2/src/vm-mqtt/communityMQTT_Message.c:142: Added slot to map 0xf7f3f180
-- MESSAGE [sys::App] running
```

`Memory address = vm->dataBaseAddr + self.offset + offset`

How is `self.offset' determined?

```c

/// src/vm-mqtt/communityMQTT_Message.c
Cell communityMQTT_Message_regSlot(SedonaVM *vm, Cell *params) {
  SessionHandle *pSession = (SessionHandle *)params[0].aval;
  uint8_t *self = params[1].aval;
  void *slot = params[2].aval;
  int16_t *paths = params[3].aval;
  uint16_t typeId = getTypeId(vm, getSlotType(vm, slot));
  uint16_t offset = getSlotHandle(vm, slot);

  void *type = getCompType(vm, self);
  const char *typeName = getTypeName(vm, type);

  log_info(
      "Register slot %p (self=%p, qn=%s, n=%s, t=%d, cb=%p (off=0x%x), db=%p, "
      "(self-db)=0x%x (%d), offset=0x%x (%d))",
      slot, self, typeName, getSlotName(vm, slot), typeId, vm->codeBaseAddr,
      ((uint8_t *)slot) - vm->codeBaseAddr, vm->dataBaseAddr,
      self - vm->dataBaseAddr, self - vm->dataBaseAddr, offset, offset);

  sys_component_on_change(changeListener);

  mqtt_add_slot_entry(pSession, (int)slot, typeId, paths);
}

// src/sys/native/sys_Component.c
Cell sys_Component_doSetInt(SedonaVM* vm, Cell* params)
{
  uint8_t* self   = params[0].aval;
  uint8_t* slot   = params[1].aval;
  int32_t val     = params[2].ival;
  uint16_t typeId = getTypeId(vm, getSlotType(vm, slot));
  uint16_t offset = getSlotHandle(vm, slot);

  switch (typeId)
  {
    case ByteTypeId:          
      if (getByte(self, offset) == val) return falseCell;
      setByte(self, offset, val);
      break;
    case ShortTypeId:
      if (getShort(self, offset) == val) return falseCell;
      setShort(self, offset, val);
      break;
    case IntTypeId:
      if (getInt(self, offset) == val) return falseCell;
      setInt(self, offset, val);
      break;
    default:
      return accessError(vm, "setInt", self, slot);
  }

  return trueCell;
}

// src/BoschCBCMW/native/sedona/sedona.h
// sys::Slot
#define getSlotName(vm, self)   getConst(vm, self, 2)
#define getSlotType(vm, self)   getConst(vm, self, 4)
#define getSlotHandle(vm, self) getShort(self, 6)

#define block2addr(cb, block) ((cb) + (block<<2))

// src/vm/vm.c
void setByte(void* self, int offset, uint8_t val)
{
  *(((uint8_t*)self) + offset) = val;
}
```
