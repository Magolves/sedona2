file(GLOB_RECURSE JAVA_SOURCES src/*.java ../sedona/src/*.java)

set(SEDONAC_CLASS_PATH ${CMAKE_SOURCE_DIR}/lib/sedona.jar)

find_jar(SEDONA_JAR sedona.jar PATHS ${CMAKE_SOURCE_DIR}/lib)

message(${SEDONAC_CLASS_PATH})

add_jar(sedonac_jar ${JAVA_SOURCES}
    ENTRY_POINT sedonac.Main
    #INCLUDE_JAR sedona_jar ${SEDONAC_CLASS_PATH}
    INCLUDE_DIR ${SEDONA_JAR}
    OUTPUT_NAME sedonac
    OUTPUT_DIR ${CMAKE_SOURCE_DIR}/lib
)