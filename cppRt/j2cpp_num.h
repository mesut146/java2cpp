#pragma once

//boxed primitives and java.lang.Number helpers
#include <cstdlib>
#include <functional>
#include <string>
#include "j2cpp_str.h"

namespace CppHelper {

//boxed primitive toString()/valueOf() helpers
inline std::string *charToString(wchar_t c) {
    return new std::string(1, static_cast<char>(c));
}
inline int comparePrim(double a, double b) { return (a > b) - (a < b); }
inline int comparePrim(int a, int b) { return (a > b) - (a < b); }
inline int comparePrim(long long a, long long b) { return (a > b) - (a < b); }

//numeric parse helpers for the boxed primitive mappers
inline double parseDouble(std::string *s) { return s ? std::strtod(s->c_str(), nullptr) : 0.0; }
inline float parseFloat(std::string *s) { return s ? std::strtof(s->c_str(), nullptr) : 0.0f; }
inline long long parseLong(std::string *s) { return s ? std::strtoll(s->c_str(), nullptr, 10) : 0; }

//java.lang.Number: holds a boxed primitive for generic/upcast positions
class Number {
    double value;
public:
    Number() : value(0) {}
    Number(int v) : value(v) {}
    Number(long v) : value(static_cast<double>(v)) {}
    Number(long long v) : value(static_cast<double>(v)) {}
    Number(float v) : value(v) {}
    Number(double v) : value(v) {}
    Number(short v) : value(v) {}
    Number(char v) : value(v) {}
    int intValue() { return static_cast<int>(value); }
    long longValue() { return static_cast<long>(value); }
    float floatValue() { return static_cast<float>(value); }
    double doubleValue() { return value; }
    short shortValue() { return static_cast<short>(value); }
    char byteValue() { return static_cast<char>(value); }
    std::string *toString() { return toStringPtr(value); }
    int hashCode() { return static_cast<int>(std::hash<double>{}(value)); }
};

//box at C++ instantiation time: the source element may be a wildcard/template type
template <typename T> Number *boxNumber(T value) {
    return new Number(value);
}

} //namespace CppHelper
