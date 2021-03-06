function(add_sedona_kit kit_name)
    add_custom_target(${kit_name} ALL 
        ${SEDONA_CC} kit.xml
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        COMMENT "Sedona: Compile kit ${kit_name}"
    )
endfunction()

function(add_sedona_native kit_name dir)
    FILE(GLOB NATIVE_FILES ${CMAKE_CURRENT_SOURCE_DIR}/${dir}/*.c ${CMAKE_CURRENT_SOURCE_DIR}/${dir}/*.h)
    #message(STATUS "Sedona: Native files ${CMAKE_CURRENT_SOURCE_DIR}/${dir}/*.c: ${NATIVE_FILES}")
    list(APPEND SEDONA_NATIVE  ${NATIVE_FILES})
    #message(STATUS "Sedona: Native files ${SEDONA_NATIVE}")
endfunction()

function(add_sedona_app app_name)
    add_custom_target(app_${app_name} ALL 
        ${SEDONA_CC} ${app_name}.sax
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        COMMENT "Sedona: Compile app ${app_name}"
    )
endfunction()

function(add_sedona_scode scode_name)
    add_custom_target(scode_${scode_name} ALL 
        ${SEDONA_CC} ${scode_name}.xml
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        COMMENT "Sedona: Compile scode ${scode_name}"
    )
endfunction()


message(STATUS "Sedona: Home is $ENV{SEDONA_HOME}")

if(DEFINED ENV{SEDONA_HOME})
    find_program(SEDONA_CC sedonac 
        NAMES sedonac sedonac.sh 
        PATHS ENV{SEDONA_HOME}/bin ${CMAKE_SOURCE_DIR}/bin ~/sedona/bin        
        DOC "Sedona compiler"
        REQUIRED)

    message(STATUS "Sedona: CC is ${SEDONA_CC}")

    # Define variables
    set(SEDONA_KITS_DIR $ENV{SEDONA_HOME}/kits)
    set(SEDONA_MANIFESTS_DIR $ENV{SEDONA_HOME}/manifests)
    set(SEDONA_VM_SOURCE_DIR $ENV{SEDONA_HOME}/src/vm)

    add_custom_target(sedona-copy-native
        COMMAND cp ${SEDONA_NATIVE} ${SEDONA_VM_SOURCE_DIR}
    )
else()
    # Sedona home not defined
    message(FATAL_ERROR "Environment variable SEDONA_HOME is not defined")
endif()

