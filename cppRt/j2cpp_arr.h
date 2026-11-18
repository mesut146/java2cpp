#pragma once

//array creation, collection factories, System.arraycopy and bit helpers
#include <cstring>
#include <initializer_list>
#include <unordered_set>
#include <vector>

namespace CppHelper {

//array creation with an initializer list: new T[]{...}
template <typename T>
std::vector<T> *from(std::initializer_list<T> values) {
    return new std::vector<T>(values);
}

//Set.of(...) -> std::unordered_set
template <typename T>
std::unordered_set<T> *fromSet(std::initializer_list<T> values) {
    return new std::unordered_set<T>(values);
}

//array creation with dimensions: new T[d1][d2]... -> arrays::makeN<T>(d1,d2...)
//stored as nested vectors of pointers, matching the generator's array access
template <typename T>
std::vector<T> *make1(int d1) {
    return new std::vector<T>(d1 > 0 ? static_cast<size_t>(d1) : 0);
}

template <typename T>
std::vector<std::vector<T> *> *make2(int d1, int d2) {
    auto outer = new std::vector<std::vector<T> *>(d1 > 0 ? static_cast<size_t>(d1) : 0);
    for (int i = 0; i < d1; ++i) outer->at(i) = make1<T>(d2);
    return outer;
}

template <typename T>
std::vector<std::vector<std::vector<T> *> *> *make3(int d1, int d2, int d3) {
    auto outer = new std::vector<std::vector<std::vector<T> *> *>(d1 > 0 ? static_cast<size_t>(d1) : 0);
    for (int i = 0; i < d1; ++i) outer->at(i) = make2<T>(d2, d3);
    return outer;
}

template <typename T>
std::vector<std::vector<std::vector<std::vector<T> *> *> *> *make4(int d1, int d2, int d3, int d4) {
    auto outer = new std::vector<std::vector<std::vector<std::vector<T> *> *> *>(d1 > 0 ? static_cast<size_t>(d1) : 0);
    for (int i = 0; i < d1; ++i) outer->at(i) = make3<T>(d2, d3, d4);
    return outer;
}

//List.toArray()
template <typename T>
std::vector<T> *toArray(std::vector<T *> *values) {
    auto result = new std::vector<T>();
    if (values != nullptr) {
        for (T *value : *values) result->push_back(value ? *value : T());
    }
    return result;
}

//System.arraycopy
template <typename T>
void arraycopy(T *src, int srcPos, T *dest, int destPos, int length) {
    if (src == nullptr || dest == nullptr || length <= 0) return;
    std::memmove(dest + destPos, src + srcPos, static_cast<size_t>(length) * sizeof(T));
}

inline int rotateLeft(int value, int distance) {
    unsigned int v = static_cast<unsigned int>(value);
    int n = distance & 31;
    if (n == 0) return value;
    return static_cast<int>((v << n) | (v >> (32 - n)));
}

inline int rotateRight(int value, int distance) {
    unsigned int v = static_cast<unsigned int>(value);
    int n = distance & 31;
    if (n == 0) return value;
    return static_cast<int>((v >> n) | (v << (32 - n)));
}

inline int signum(int value) { return (value > 0) - (value < 0); }

inline int reverseBytes(int value) {
    unsigned int v = static_cast<unsigned int>(value);
    return static_cast<int>(((v & 0x000000FFu) << 24) | ((v & 0x0000FF00u) << 8) |
                            ((v & 0x00FF0000u) >> 8) | ((v & 0xFF000000u) >> 24));
}

inline int bitCount(int value) {
    unsigned int v = static_cast<unsigned int>(value);
    int count = 0;
    for (; v != 0; v &= v - 1) ++count;
    return count;
}

inline int numberOfTrailingZeros(int value) {
    unsigned int v = static_cast<unsigned int>(value);
    if (v == 0) return 32;
    int count = 0;
    while ((v & 1u) == 0) { v >>= 1; ++count; }
    return count;
}

inline int numberOfLeadingZeros(int value) {
    unsigned int v = static_cast<unsigned int>(value);
    if (v == 0) return 32;
    int count = 0;
    unsigned int mask = 1u << 31;
    while ((v & mask) == 0) { ++count; mask >>= 1; }
    return count;
}

} //namespace CppHelper

//older generated code refers to the array helpers as arrays::*
namespace arrays = CppHelper;
