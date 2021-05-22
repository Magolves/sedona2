cmake_minimum_required(VERSION 3.15)

project(Sedona)

option(COMPILE_KITS Off)

include(cmake/Java.cmake)
include(cmake/Sedona.cmake)

add_subdirectory(src/sedona/src)
add_subdirectory(src/sedonac)

add_subdirectory(src/launcher)

add_subdirectory(src/sys)
add_subdirectory(src/basicSchedule)
add_subdirectory(src/inet)
add_subdirectory(src/datetime)
add_subdirectory(src/datetimeStd)
if (COMPILE_KITS)
    add_subdirectory(src/func)
    add_subdirectory(src/pricomp)
    add_subdirectory(src/pstore)
    add_subdirectory(src/control)
    add_subdirectory(src/types)
    add_subdirectory(src/math)
    add_subdirectory(src/sox)
    add_subdirectory(src/web)
    add_subdirectory(src/hvac)
    add_subdirectory(src/logic)
    add_subdirectory(src/timing)
endif()

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

add_subdirectory(src/vm-staged)
add_subdirectory(src/vm-mqtt)

# Buold scode files
add_subdirectory(scode)
# Build the apps
add_subdirectory(apps)

enable_testing()