
```text
[Generate] Slot mqtteval::MqttComponent.diff:40|8548
[Generate] Slot mqtteval::MqttComponent.isHeating:36|8556
[Generate] Slot mqtteval::MqttComponent.sp:44|8564
[Generate] Slot mqtteval::MqttComponent.cv:48|8572
[Generate] Slot mqtteval::MqttComponent.out:37|8580
[Generate] Slot mqtteval::MqttComponent.raise:38|8588
[Generate] Slot mqtteval::MqttComponent.lower:39|8596
```

```text
-- MESSAGE [communityMQTT::MQTTService] MQTT service started
22:16:30 INFO  /home/oliver/dev/sedona2/src/vm-mqtt/communityMQTT_Message.c:136: Register slot 0xf7f9a188 (self=0x91f2e00, qn=MqttComponent, n=cv, t=6, cb=0xf7f98010 (off=0x2178), db=0x91ee700, (self-db)=0x4700 (18176), offset=0x30 (48))
22:16:30 INFO  /home/oliver/dev/sedona2/src/vm-mqtt/communityMQTT_Worker.c:305:  * [MQTTService] MQTT worker thread started 91f18b0
22:16:30 INFO  /home/oliver/dev/sedona2/src/vm-mqtt/communityMQTT_Message.c:141: Added slot to map 0xf7f9a188
22:16:30 INFO  /home/oliver/dev/sedona2/src/vm-mqtt/communityMQTT_Message.c:136: Register slot 0xf7f9a180 (self=0x91f2e00, qn=MqttComponent, n=sp, t=6, cb=0xf7f98010 (off=0x2170), db=0x91ee700, (self-db)=0x4700 (18176), offset=0x2c (44))
22:16:30 INFO  /home/oliver/dev/sedona2/src/vm-mqtt/communityMQTT_Message.c:141: Added slot to map 0xf7f9a180
-- MESSAGE [sys::App] running
```

`Memory address = vm->dataBaseAddr + self.offset + offset`

How is `self.offset' determined?

```c
void setByte(void* self, int offset, uint8_t val)
{
  *(((uint8_t*)self) + offset) = val;
}
```
