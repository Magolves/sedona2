# Verbose make for debugging
set(CMAKE_VERBOSE_MAKEFILE OFF)
set(CMAKE_BUILD_TYPE Debug)


SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -m32")
SET(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -m32")
SET(CMAKE_SHARED_LINK_FLAGS "${CMAKE_SHARED_LINK_FLAGS} -m32")
SET(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -m32")


set(SRC
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
    main.c
    nativetable.c
    scode.h
    sedona.h
    sedonaPlatform.h
    sys_Component.c
    sys_File_std.c
    sys_FileStore_std.c
    #sys_PlatformService_unix.c
    sys_StdOutStream_std.c
    sys_Str.c
    sys_Sys.c
    sys_Sys_std.c
    sys_Sys_unix.c
    sys_Test.c
    sys_Type.c
    vm.c
)

set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

if (APPLE)
    set(SRC ${SRC} sys_PlatformService_macos.c)
else()
    set(SRC ${SRC} sys_PlatformService_unix.c)
endif()

add_executable(svm ${SRC})
target_compile_options(svm PRIVATE -m32)

target_link_options(svm PRIVATE -m32)
target_link_libraries(svm PRIVATE pthread)

target_compile_definitions(svm PRIVATE -D__UNIX__ -DPLAT_BUILD_VERSION="2.0.0" -DPLATFORM_ID="${CMAKE_SYSTEM}" -DSOCKET_FAMILY_INET)
if (APPLE)
    target_compile_definitions(svm PRIVATE -D__APPLE__)
endif()

install(TARGETS svmt DESTINATION $ENV{SEDONA_HOME}/bin)