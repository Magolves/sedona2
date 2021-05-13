cmake_minimum_required(VERSION 3.15)

project(Sedona)

include(cmake/Java.cmake)
include(cmake/Sedona.cmake)

add_subdirectory(src/sedona/src)
add_subdirectory(src/sedonac)

add_subdirectory(src/launcher)

add_subdirectory(src/sys)
add_subdirectory(src/inet)
add_subdirectory(src/datetime)
add_subdirectory(src/datetimeStd)

add_subdirectory(platforms/src/generic/unix)

set (VM_DIR ${CMAKE_SOURCE_DIR}/src/vm-test)
set(PLATFORM_DEF platforms/src/generic/unix/generic-unix.xml)

message(STATUS "${SEDONA_CC} ARGS ${PLATFORM_DEF} -outDir ${VM_DIR}")

add_custom_target(staging ALL
    COMMAND ${SEDONA_CC} ${PLATFORM_DEF} -outDir ${VM_DIR}
    WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
    DEPENDS sys inet datetime datetimeStd platUnix sedonac_jar
    #BYPRODUCTS ${VM_DIR}/sedona.h 
    COMMENT "Prepare platform"
)

add_subdirectory(src/vm-mqtt)

enable_testing()