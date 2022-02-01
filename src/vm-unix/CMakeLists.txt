project (middleware-native)
cmake_minimum_required(VERSION 3.16)

set(CMAKE_EXPORT_COMPILE_COMMANDS On)
set(CMAKE_BUILD_TYPE Debug)
set(MQTT_SOURCES mqtt_client.c mqtt_callbacks.c mqtt_map.c log.c constants.c)
set(MQTT_STUB_SOURCES local/main.c sedona/vmstub.c)
set(VM_SOURCES 
    constants.c
    datetimeStd_DateTimeServiceStd.c
    inet_Crypto_sha1.c
    inet_sha1.c
    inet_TcpServerSocket_std.c
    inet_TcpSocket_std.c
    inet_UdpSocket_std.c
    inet_util_std.c
    main.c
    nativetable.c
    sys_Component.c
    sys_File_std.c
    sys_FileStore_std.c
    sys_PlatformService_unix.c
    sys_StdOutStream_std.c
    sys_Str.c
    sys_Sys.c
    sys_Sys_std.c
    sys_Sys_unix.c
    sys_Test.c
    sys_Type.c
    vm.c
)

if (LOCAL_TEST)
    # Local test executable
    set(SOURCES ${MQTT_SOURCES} ${MQTT_STUB_SOURCES})
    set(TARGET_NAME mqtt)
else()
    # Prefer static linkage
    set(CMAKE_FIND_LIBRARY_SUFFIXES .a .lib)
    # Build the VM    
    set(SOURCES ${MQTT_SOURCES} ${VM_SOURCES})
    set(TARGET_NAME svm-mqtt)
endif()

add_executable(${TARGET_NAME} ${SOURCES})

target_link_libraries(${TARGET_NAME} PUBLIC mosquitto)
target_include_directories(${TARGET_NAME} PRIVATE sedona .)

if(WINDOWS)
    target_compile_definitions(${TARGET_NAME} PRIVATE -D__WINDOWS__)
else()
    target_compile_definitions(${TARGET_NAME} PRIVATE -D__UNIX__ -DLOG_USE_COLOR)
endif()

if (NOT LOCAL_TEST)
    target_compile_definitions(${TARGET_NAME} PRIVATE -DPLAT_BUILD_VERSION="2.0.0" -DSOCKET_FAMILY_INET)
    target_compile_options(${TARGET_NAME} PRIVATE -m32)
    target_link_options(${TARGET_NAME} PRIVATE -m32)    
endif()