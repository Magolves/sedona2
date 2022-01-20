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


## Linux
### Build the VM on Linux

For Sedona `libmosquitto` needs to be build as 32bit-library (and sacrifice some features)

In the top level `CmakeLists.txt` add linker and compile flag `-m32`

```cmake
# ...
option(DOCUMENTATION "Build documentation?" OFF)

# Compile and link 32bit library
add_link_options(-m32)
add_compile_options(-m32)

add_subdirectory(lib)
if (WITH_CLIENTS)
# ...
```

```bash
# Install openSSL libs for 32bit
$sudo apt install openssl:i386 libssl-dev:i386
$cmake .. -B . -DWITH_TLS=Off -DWITH_CLIENTS=Off -DWITH_BROKER=Off -DWITH_PLUGINS=Off -DWITH_APPS=Off -DCMAKE_INSTALL_PREFIX=~/dev/mosquitto/i386
```

Add the install path to the library search path

```bash
export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:~/dev/mosquitto/i386/lib"
```

and update library cache

```bash
$sudo ldconfig
```

Then the VM should run

```bash
$svm-mqtt scode/platUnix.scode apps/unix/platUnix.sab

Sedona VM 2.0.0
buildDate: Jan 20 2022 23:27:10
endian:    little
blockSize: 4
refSize:   4

-- MESSAGE [sys::App] starting
-- MESSAGE [sox::SoxService] started port=1876
-- MESSAGE [sox::SoxService] DASP Discovery enabled
-- MESSAGE [web::WebService] started port=8080
-- MESSAGE [sys::App] running
```
