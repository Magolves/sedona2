file(GLOB_RECURSE JAVA_SOURCES *.java)


add_jar(sedona_jar ${JAVA_SOURCES} version.txt
    OUTPUT_NAME sedona
    OUTPUT_DIR OUTPUT_DIR ${CMAKE_SOURCE_DIR}/lib
)
