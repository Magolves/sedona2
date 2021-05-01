file(GLOB_RECURSE JAVA_SOURCES src/*.java)

add_jar(sedona_jar ${JAVA_SOURCES} src/version.txt
    OUTPUT_NAME sedona
    OUTPUT_DIR ${CMAKE_SOURCE_DIR}/lib
)