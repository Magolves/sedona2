find_package(Java COMPONENTS Development)
# Load mdoule to use java commands like 'add_jar'
include(UseJava)

set(CMAKE_JAVA_COMPILE_FLAGS -nowarn)