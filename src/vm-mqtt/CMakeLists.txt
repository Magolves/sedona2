cmake_minimum_required(VERSION 3.15)

project(vm-mqtt)

# Verbose make for debugging
set(CMAKE_VERBOSE_MAKEFILE OFF)
set(CMAKE_BUILD_TYPE Debug)


SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -m32")
SET(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -m32")
SET(CMAKE_SHARED_LINK_FLAGS "${CMAKE_SHARED_LINK_FLAGS} -m32")
SET(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -m32")

set(CMAKE_BUILD_TYPE Debug)


add_definitions(-D__UNIX__ -DPLAT_BUILD_VERSION="3.21" -DPLATFORM_ID="${CMAKE_SYSTEM}" -DSOCKET_FAMILY_INET)

set(SRC
    communityMQTT_common.c
    communityMQTT_common.h
    communityMQTT_Message.c
    communityMQTT_Worker.c
    datetimeStd_DateTimeServiceStd.c
    errorcodes.h
    inet_Crypto_sha1.c
    inet_sha1.c
    inet_sha1.h
    inet_TcpServerSocket_std.c
    inet_TcpSocket_std.c
    inet_UdpSocket_std.c
    inet_util.h
    inet_util_std.c
    inet_util_std.h
    log.c
    log.h
    main.c
    MQTTClient.c
    MQTTClient.h
    MQTTConnectClient.c
    MQTTConnect.h
    MQTTConnectServer.c
    MQTTDeserializePublish.c
    MQTTFormat.c
    MQTTFormat.h
    MQTTLinux.c
    MQTTLinux.h
    MQTTMap.c
    MQTTMap.h
    MQTTPacket.c
    MQTTPacket.h
    MQTTPublish.h
    MQTTSerializePublish.c
    MQTTSubscribeClient.c
    MQTTSubscribe.h
    MQTTSubscribeServer.c
    MQTTUnsubscribeClient.c
    MQTTUnsubscribe.h
    MQTTUnsubscribeServer.c
    nativetable.c
    scode.h
    sedona.h
    sedonaPlatform.h
    StackTrace.h
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
    uthash.h
    vm.c
)

add_executable(vmmq ${SRC})

target_link_libraries(vmmq PRIVATE pthread)