#pragma once

//java.util.Objects and value semantics for generated records
#include <cstdint>
#include <cstring>
#include <functional>
#include <string>
#include "j2cpp_str.h"

namespace CppHelper {

//java.util.Objects helpers (null-safe)
template <typename T> bool objEquals(T *a, T *b) {
    return a == b || (a && b && a->equals(b));
}
inline bool objEquals(std::string *a, std::string *b) {
    return a == b || (a && b && *a == *b);
}
template <typename T> int objHash(T *p) { return p ? p->hashCode() : 0; }
inline int objHash(std::string *p) { return p ? static_cast<int>(std::hash<std::string>{}(*p)) : 0; }
template <typename T> std::string *objToString(T *p) {
    return new std::string(p ? *p->toString() : std::string("null"));
}
inline std::string *objToString(std::string *p) {
    return new std::string(p ? *p : std::string("null"));
}

//value semantics for record components
inline std::string valueToString(std::string *s) { return s ? *s : std::string("null"); }
inline std::string valueToString(const std::string *s) { return s ? *s : std::string("null"); }
inline std::string valueToString(bool b) { return b ? "true" : "false"; }
template <typename T> std::string valueToString(T value) { return detail::toString(value); }
//pointer to a generated object: call its toString()
template <typename T> std::string valueToString(T *p) {
    return p ? *p->toString() : std::string("null");
}

//value equality for record components
inline bool eq(bool a, bool b) { return a == b; }
inline bool eq(std::string *a, std::string *b) {
    return a == b || (a && b && *a == *b);
}
inline bool eq(const std::string *a, const std::string *b) {
    return a == b || (a && b && *a == *b);
}
template <typename T> bool eq(T a, T b) { return a == b; }
template <typename T> bool eq(T *a, T *b) { return a == b || (a && b && a->equals(b)); }

//hash of a single value
inline int hashValue(bool b) { return b ? 1231 : 1237; }
inline int hashValue(int v) { return v; }
inline int hashValue(long v) { return static_cast<int>(v ^ (v >> 32)); }
inline int hashValue(double v) {
    uint64_t bits; std::memcpy(&bits, &v, sizeof(bits));
    return static_cast<int>(bits ^ (bits >> 32));
}
inline int hashValue(float v) {
    uint32_t bits; std::memcpy(&bits, &v, sizeof(bits));
    return static_cast<int>(bits);
}
inline int hashValue(std::string *s) {
    return s ? static_cast<int>(std::hash<std::string>{}(*s)) : 0;
}
inline int hashValue(const std::string *s) {
    return s ? static_cast<int>(std::hash<std::string>{}(*s)) : 0;
}
template <typename T> int hashValue(T *p) { return p ? p->hashCode() : 0; }

template <typename T> int hashCombine(T value) { return hashValue(value); }
template <typename T, typename... Rest>
int hashCombine(T first, Rest... rest) {
    int r = hashValue(first);
    using expander = int[];
    (void)expander{0, (r = 31 * r + hashValue(rest), 0)...};
    return r;
}
inline int hashCombine() { return 1; }

} //namespace CppHelper
