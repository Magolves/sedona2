project (middleware-native)

set(CMAKE_EXPORT_COMPILE_COMMANDS On)

add_executable(cbcmw mqtt_client.c local/main.c)

target_link_libraries(cbcmw PUBLIC mosquitto)
target_include_directories(cbcmw PRIVATE ../../vm)

if(WINDOWS)
else()
target_compile_definitions(cbcmw PRIVATE -D__UNIX__)
endif()