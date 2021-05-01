cmake_minimum_required(VERSION 3.15)

project(Sedona)

include(cmake/Java.cmake)
include(cmake/Sedona.cmake)

add_subdirectory(src/sedona)
add_subdirectory(src/sedonac)

add_subdirectory(src/sys)
add_subdirectory(src/inet)

add_subdirectory(src/vm-mqtt)