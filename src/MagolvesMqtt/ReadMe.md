# CBC Middleware

## Mosquitto client implementation

- [mosquitto.h](<https://mosquitto.org/api/files/mosquitto-h.html>)
- [Hints on MQTT client implementation](<https://www.bevywise.com/mqtt/developer-guide/>)
- [MQTT client in C](<https://github.com/eclipse/mosquitto/blob/master/client/pub_client.c>)
- [MQTT config struct](<https://github.com/eclipse/mosquitto/blob/master/client/client_shared.h>)

### Connect

#### Synchronous

```c
rc = mosquitto_connect(mosq, host, port, 60);
if (rc != MOSQ_ERR_SUCCESS) {
    if (rc == MOSQ_ERR_INVAL) {
    printf("ERROR: Invalid parameters %s (%d)\n", host, port);
    } else {
    printf("ERROR: Illegal call (rc = %d)\n", rc);
    }
}
```

#### Asynchronous

```c
// Start thread
mosquitto_loop_start(mosq);
// Connect asynchronously 
rc = mosquitto_connect_async(mosq, host, port, -1);

// ... do something

// disconnect session
mosquitto_disconnect(mosq);
// stop thread
mosquitto_loop_stop(mosq);
```



## JSON

- [JSON examples](<https://kezunlin.me/post/f3c3eb8/>)

## Tools

- [Logging with Python](<http://www.steves-internet-guide.com/simple-python-mqtt-data-logger/>)
- [MQTT Explorer](<https://mqtt-explorer.com/>)
