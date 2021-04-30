function(add_sedona_kit kit_name)
    add_custom_target(kit_name ALL 
        ${SEDONA_CC} kit.xml
        #WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        COMMENT "Sedona: Compile kit ${kit_name}"
    )

endfunction()

message(STATUS "Sedona: Home is $ENV{sedona_home}")

if(DEFINED ENV{sedona_home})
    find_program(SEDONA_CC sedonac 
        NAMES sedonac sedonac.sh 
        PATHS ~/sedona ENV sedona_home
        DOC "Sedona compiler"
        REQUIRED)

    message(STATUS "Sedona: CC is ${SEDONA_CC}")

    set(SEDONA_KITS_DIR $ENV{sedona_home}/kits)
    set(SEDONA_MANIFESTS_DIR $ENV{sedona_home}/manifests)
else()
    # Sedona home not defined
    message(FATAL_ERROR "Environment variable SEDONA_HOME is not defined")
endif()

