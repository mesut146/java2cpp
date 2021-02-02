cmake_minimum_required (VERSION 3.0)

set(CMAKE_VERBOSE_MAKEFILE ON)


add_executable(ptr func-ptr.cpp)

target_include_directories(ptr PUBLIC "/home/mesut/Desktop/rttr-master/src" "/home/mesut/Desktop/rttr-master/build/src")
    
target_link_libraries(ptr PUBLIC "/home/mesut/Desktop/rttr-master/build/lib/librttr_core.so.0.9.7")
