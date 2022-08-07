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
    if(WIN32)
        add_custom_target(all-kits ALL 
            DEPENDS sedonacmt_jar
            WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
            COMMAND ${CMAKE_SOURCE_DIR}/bin/sedonacmt.exe ${CMAKE_SOURCE_DIR}/src
        )
    else()
        add_custom_target(all-kits ALL 
            DEPENDS sedonacmt_jar
            WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
            COMMAND ${CMAKE_SOURCE_DIR}/bin/sedonacmt.sh ${CMAKE_SOURCE_DIR}/src
        )
    endif()
else()
    add_subdirectory(src/sys)
    #add_subdirectory(src/basicSchedule)
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
endif()

if (WIN32)
    set(SEDONA_PLATFORM platWin32)
    add_subdirectory(platforms/src/generic/win32)

    set (VM_DIR ${CMAKE_SOURCE_DIR}/src/vm-win32)
    set(PLATFORM_DEF platforms/src/generic/win32/generic-win32.xml)
else()
    set(SEDONA_PLATFORM platUnix)
    add_subdirectory(platforms/src/generic/unix)

    set (VM_DIR ${CMAKE_SOURCE_DIR}/src/vm-unix)
    set(PLATFORM_DEF platforms/src/generic/unix/generic-unix.xml)
endif()

message(STATUS "${SEDONA_CC} ARGS ${PLATFORM_DEF} -outDir ${VM_DIR}")

if (USE_SEDONA_MT)
    add_custom_target(staging ALL
        COMMAND ${SEDONA_CC} ${PLATFORM_DEF} -outDir ${VM_DIR}
        WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
        DEPENDS all-kits ${SEDONA_PLATFORM} sedonacmt_jar
        COMMENT "Prepare platform"
    )
else()    
    add_custom_target(staging ALL
        COMMAND ${SEDONA_CC} ${PLATFORM_DEF} -outDir ${VM_DIR}
        WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
        DEPENDS sys inet datetime datetimeStd ${SEDONA_PLATFORM} sedonac_jar
        COMMENT "Prepare platform"
    )
    
    if (UNIX)
        add_subdirectory(src/vm-unix)
    endif()
endif()

#add_subdirectory(src/vm-test)

# Build scode files
add_subdirectory(scode)
# Build the apps
add_subdirectory(apps)