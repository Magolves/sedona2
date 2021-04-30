cmake_minimum_required(VERSION 3.15)

project(Sedona)

include(cmake/Sedona.cmake)

add_subdirectory(src/sys)
add_subdirectory(src/inet)