#file(GLOB_RECURSE JAVA_SOURCES src/*.java ../sedona/src/*.java)
file(GLOB_RECURSE JAVA_SOURCES src/*.java)

set(SEDONAC_CLASS_PATH ${CMAKE_BINARY_DIR}/lib/sedona.jar)

find_jar(SEDONA_JAR sedona.jar PATHS ${CMAKE_SOURCE_DIR}/lib)

message(${SEDONAC_CLASS_PATH})

add_jar(sedonac_jar ${JAVA_SOURCES}
    ENTRY_POINT sedonac.Main
    INCLUDE_JARS sedona_jar    
    OUTPUT_NAME sedonac
    OUTPUT_DIR ${CMAKE_BINARY_DIR}/lib/${CMAKE_SYSTEM_NAME}
)

install_jar(sedonac_jar DESTINATION ${CMAKE_BINARY_DIR}/lib)