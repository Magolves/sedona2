cmake_minimum_required(VERSION 3.15)

project(Sedona)

option(COMPILE_KITS Off)

include(cmake/Java.cmake)
include(cmake/Sedona.cmake)


enable_testing()

add_subdirectory(src/sedona/src)
add_subdirectory(src/sedonac)

add_subdirectory(src/launcher)

if (USE_SEDONA_MT)
    add_custom_target(all-kits ALL 
        DEPENDS sedonacmt_jar
        WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
        COMMAND ${CMAKE_SOURCE_DIR}/bin/sedonacmt.sh ${CMAKE_SOURCE_DIR}/src
    )
else()
    add_subdirectory(src/sys)
    add_subdirectory(src/basicSchedule)
    add_subdirectory(src/inet)
    add_subdirectory(src/datetime)
    add_subdirectory(src/datetimeStd)
    add_subdirectory(src/sedona-mqtt)
    add_subdirectory(src/mqtteval)
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
endif()
add_subdirectory(platforms/src/generic/unix)

set (VM_DIR ${CMAKE_SOURCE_DIR}/src/vm-test)
set(PLATFORM_DEF platforms/src/generic/unix/generic-unix.xml)

message(STATUS "${SEDONA_CC} ARGS ${PLATFORM_DEF} -outDir ${VM_DIR}")

if (USE_SEDONA_MT)
    add_custom_target(staging ALL
        COMMAND ${SEDONA_CC} ${PLATFORM_DEF} -outDir ${VM_DIR}
        WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
        DEPENDS all-kits platUnix sedonacmt_jar
        #BYPRODUCTS ${VM_DIR}/sedona.h 
        COMMENT "Prepare platform"
    )
else()    
    add_custom_target(staging ALL
        COMMAND ${SEDONA_CC} ${PLATFORM_DEF} -outDir ${VM_DIR}
        WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
        DEPENDS sys inet datetime datetimeStd platUnix sedonac_jar
        #BYPRODUCTS ${VM_DIR}/sedona.h 
        COMMENT "Prepare platform"
    )
endif()

add_subdirectory(src/vm-staged)
add_subdirectory(src/vm-mqtt)
#add_subdirectory(src/vm-test)

# Buold scode files
add_subdirectory(scode)
# Build the apps
add_subdirectory(apps)