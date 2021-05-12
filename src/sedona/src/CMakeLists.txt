file(GLOB_RECURSE JAVA_SOURCES *.java)

set(CMAKE_JAVA_COMPILE_FLAGS -nowarn)


add_jar(sedona_jar ${JAVA_SOURCES} version.txt
    OUTPUT_NAME sedona
    OUTPUT_DIR OUTPUT_DIR ${CMAKE_BINARY_DIR}/lib/${CMAKE_SYSTEM_NAME}
)

install_jar(sedona_jar DESTINATION ${CMAKE_BINARY_DIR}/lib)
