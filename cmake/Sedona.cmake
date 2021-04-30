function(add_sedona_kit kit_name)
    add_custom_target(${kit_name} ALL 
        ${SEDONA_CC} kit.xml
        #WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        COMMENT "Sedona: Compile kit ${kit_name}"
    )
endfunction()


function(add_sedona_native kit_name dir)
    FILE(GLOB NATIVE_FILES ${CMAKE_CURRENT_SOURCE_DIR}/${dir}/*.c ${CMAKE_CURRENT_SOURCE_DIR}/${dir}/*.h)
    #message(STATUS "Sedona: Native files ${CMAKE_CURRENT_SOURCE_DIR}/${dir}/*.c: ${NATIVE_FILES}")
    list(APPEND SEDONA_NATIVE  ${NATIVE_FILES})
    #message(STATUS "Sedona: Native files ${SEDONA_NATIVE}")
endfunction()

message(STATUS "Sedona: Home is $ENV{sedona_home}")

if(DEFINED ENV{sedona_home})
    find_program(SEDONA_CC sedonac 
        NAMES sedonac sedonac.sh 
        PATHS ~/sedona ENV sedona_home
        DOC "Sedona compiler"
        REQUIRED)

    message(STATUS "Sedona: CC is ${SEDONA_CC}")

    # Define variables
    set(SEDONA_KITS_DIR $ENV{sedona_home}/kits)
    set(SEDONA_MANIFESTS_DIR $ENV{sedona_home}/manifests)
    set(SEDONA_VM_SOURCE_DIR $ENV{sedona_home}/src/vm)

    add_custom_target(sedona-copy-native
        COMMAND cp ${SEDONA_NATIVE} ${SEDONA_VM_SOURCE_DIR}
    )
else()
    # Sedona home not defined
    message(FATAL_ERROR "Environment variable SEDONA_HOME is not defined")
endif()

