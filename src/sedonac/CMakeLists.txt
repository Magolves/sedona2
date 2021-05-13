#file(GLOB_RECURSE JAVA_SOURCES src/*.java ../sedona/src/*.java)
file(GLOB_RECURSE JAVA_SOURCES src/*.java)

find_jar(SEDONA_JAR sedona.jar PATHS ${CMAKE_SOURCE_DIR}/lib)

add_jar(sedonac_jar ${JAVA_SOURCES}
    ENTRY_POINT sedonac.Main
    INCLUDE_JARS sedona_jar    
    OUTPUT_NAME sedonac
    OUTPUT_DIR ${CMAKE_SOURCE_DIR}/lib
)

add_test(NAME sedonac_from_jar COMMAND java -jar ${CMAKE_SOURCE_DIR}/lib/sedonac.jar -version)
