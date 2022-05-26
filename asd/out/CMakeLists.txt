cmake_minimum_required(VERSION 2.8.11)

project(myproject)

add_library(mylib STATIC
)
include_directories(
  ${CMAKE_SOURCE_DIR}
  ${CMAKE_SOURCE_DIR}/lib
)
