project (middleware-native)
cmake_minimum_required(VERSION 3.16)

set(CMAKE_EXPORT_COMPILE_COMMANDS On)

add_executable(cbcmw mqtt_client.c local/main.c sedona/vmstub.c)

target_link_libraries(cbcmw PUBLIC mosquitto)
target_include_directories(cbcmw PRIVATE sedona .)

if(WINDOWS)
target_compile_definitions(cbcmw PRIVATE -D__WINDOWS__)
else()
target_compile_definitions(cbcmw PRIVATE -D__UNIX__)
endif()