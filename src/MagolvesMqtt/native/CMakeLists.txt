project (middleware-native)
cmake_minimum_required(VERSION 3.16)

set(CMAKE_EXPORT_COMPILE_COMMANDS On)

add_executable(mqtt-mosquitto mqtt_client.c mqtt_callbacks.c constants.c local/main.c sedona/vmstub.c)

target_link_libraries(mqtt-mosquitto PUBLIC mosquitto)
target_include_directories(mqtt-mosquitto PRIVATE sedona .)

if(WINDOWS)
target_compile_definitions(mqtt-mosquitto PRIVATE -D__WINDOWS__)
else()
target_compile_definitions(mqtt-mosquitto PRIVATE -D__UNIX__)
endif()