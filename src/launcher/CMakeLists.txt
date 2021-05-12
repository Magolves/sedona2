project(sedona-bin)

cmake_minimum_required(VERSION 3.15)

set(CMAKE_VERBOSE_MAKEFILE OFF)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
set(CMAKE_BUILD_TYPE Release)

# ---------------------- Check for Java installation
set(JAVA_HOME $ENV{JAVA_HOME})
if (NOT JAVA_HOME) 
    message(ERROR "JAVA_HOME not defined")
else()
    message(STATUS "Java found at ${JAVA_HOME}")
endif()    

# ---------------------- Determine install directory
set(SEDONA_HOME $ENV{SEDONA_HOME})
if (NOT SEDONA_HOME) 
  message(WARNING "SEDONA_HOME is not defined, install to ${CMAKE_CURRENT_SOURCE_DIR}/../../bin")
  set(INSTALL_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../bin)
else()
    message(STATUS "Sedona found at ${SEDONA_HOME}")
    set(INSTALL_DIR ${SEDONA_HOME}/bin)  
endif()

# ---------------------- Retrieve git version
message(STATUS "Resolving GIT Version")

set(GIT_REVISION "unknown")

find_package(Git)
if(GIT_FOUND)
  execute_process(
    COMMAND ${GIT_EXECUTABLE} rev-parse --short HEAD
    WORKING_DIRECTORY "${local_dir}"
    OUTPUT_VARIABLE GIT_REVISION
    ERROR_QUIET
    OUTPUT_STRIP_TRAILING_WHITESPACE
  )
  message( STATUS "GIT hash: ${GIT_REVISION}")
else()
  message(STATUS "GIT not found")
endif()


string(TIMESTAMP TS "%Y-%m-%d %H:%M")
add_definitions( -DLAUNCHER_VERSION="${GIT_REVISION}" -DLAUNCHER_DATE="${TS}")

# ---------------------- Define targets
# To enable debug, you neet so set the environment variable 'sedona_launcher_debug' to any value or pass -v option
# In PowerShell you cann accomplish this via
# > $env:sedona_launcher_debug=1

add_executable(sedonac-rb launcher.cpp)
# Define your main class here (NOTE: You to use '/' as path separator, not '.'!)
target_compile_definitions(sedonac-rb PRIVATE -DLAUNCHER_MAIN="sedonacmt/MainMulti" -DSEDONAC_JAR="sedonac-rb")
target_include_directories(sedonac-rb PRIVATE ${JAVA_HOME}/include ${JAVA_HOME}/include/win32)

# Target for the default (fallback) compiler
add_executable(sedonac launcher.cpp)
target_compile_definitions(sedonac PRIVATE -DLAUNCHER_MAIN="sedonac/Main" -DSEDONAC_JAR="sedonac")
target_include_directories(sedonac PRIVATE ${JAVA_HOME}/include ${JAVA_HOME}/include/win32)

# Install both target to binary directory
install(TARGETS sedonac-rb RUNTIME DESTINATION ${INSTALL_DIR})
install(TARGETS sedonac RUNTIME DESTINATION ${INSTALL_DIR})