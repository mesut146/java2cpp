#pragma once

//helpers referenced by the bundled cpp mappers (String.format/replace/split, etc.)
//header-only so generated sources only need this file on the include path

#include <algorithm>
#include <cctype>
#include <charconv>
#include <cstdio>
#include <cstring>
#include <cstdint>
#include <cstdlib>
#include <cmath>
#include <cwctype>
#include <filesystem>
#include <fstream>
#include <functional>
#include <initializer_list>
#include <iostream>
#include <iterator>
#include <optional>
#include <random>
#include <type_traits>
#include <memory>
#include <stdexcept>
#include <string>
#include <unordered_set>
#include <vector>

namespace CppHelper {

//java.util.Random / Math.random() backend
inline std::mt19937_64 &rng() {
    static std::mt19937_64 engine(std::random_device{}());
    return engine;
}
inline double nextDouble() {
    return std::generate_canonical<double, 53>(rng());
}
inline int nextInt() { return static_cast<int>(rng()()); }
inline int nextInt(int bound) {
    if (bound <= 0) return 0;
    return static_cast<int>(rng()() % static_cast<unsigned long long>(bound));
}
inline long long nextLong() { return static_cast<long long>(rng()()); }
inline bool nextBoolean() { return (rng()() & 1) != 0; }
inline double nextGaussian() { return std::normal_distribution<double>(0.0, 1.0)(rng()); }
class Random {
public:
    int nextInt() { return CppHelper::nextInt(); }
    int nextInt(int bound) { return CppHelper::nextInt(bound); }
    long long nextLong() { return CppHelper::nextLong(); }
    double nextDouble() { return CppHelper::nextDouble(); }
    bool nextBoolean() { return CppHelper::nextBoolean(); }
};
inline std::string *randomUUID() {
    static const char *hex = "0123456789abcdef";
    std::string s = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";
    for (char &c : s) {
        if (c == 'x') c = hex[nextInt(16)];
        else if (c == 'y') c = hex[8 + nextInt(4)];
    }
    return new std::string(s);
}

template <typename T> int signum(T v) { return (v > 0) - (v < 0); }

//boxed primitive toString()/valueOf() helpers
inline std::string *charToString(wchar_t c) {
    return new std::string(1, static_cast<char>(c));
}
inline int comparePrim(double a, double b) { return (a > b) - (a < b); }
inline int comparePrim(int a, int b) { return (a > b) - (a < b); }
inline int comparePrim(long long a, long long b) { return (a > b) - (a < b); }

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

//numeric parse helpers for the boxed primitive mappers
inline double parseDouble(std::string *s) { return s ? std::strtod(s->c_str(), nullptr) : 0.0; }
inline float parseFloat(std::string *s) { return s ? std::strtof(s->c_str(), nullptr) : 0.0f; }
inline long long parseLong(std::string *s) { return s ? std::strtoll(s->c_str(), nullptr, 10) : 0; }

//Java String helpers used by the bundled mapper
inline std::string *join(std::string *delim, std::initializer_list<std::string *> parts) {
    std::string out;
    bool first = true;
    for (std::string *p : parts) {
        if (!first && delim) out += *delim;
        first = false;
        if (p) out += *p;
    }
    return new std::string(out);
}

inline std::string *join(std::string *delim, std::vector<std::string *> *parts) {
    std::string out;
    bool first = true;
    if (parts) for (std::string *p : *parts) {
        if (!first && delim) out += *delim;
        first = false;
        if (p) out += *p;
    }
    return new std::string(out);
}

inline std::string *repeat(std::string *s, int count) {
    std::string out;
    if (s) for (int i = 0; i < count; ++i) out += *s;
    return new std::string(out);
}

inline std::string *strip(std::string *s) {
    if (!s) return new std::string();
    auto isSpace = [](unsigned char c) { return std::isspace(c) != 0; };
    auto b = s->begin(), e = s->end();
    while (b != e && isSpace(static_cast<unsigned char>(*b))) ++b;
    while (e != b && isSpace(static_cast<unsigned char>(*(e - 1)))) --e;
    return new std::string(b, e);
}

inline bool isBlank(std::string *s) {
    if (!s) return true;
    for (char c : *s) if (!std::isspace(static_cast<unsigned char>(c))) return false;
    return true;
}

inline int compareIgnoreCase(std::string *a, std::string *b) {
    std::string x = a ? *a : std::string(), y = b ? *b : std::string();
    for (std::size_t i = 0; i < x.size() && i < y.size(); ++i) {
        int d = std::tolower(static_cast<unsigned char>(x[i]))
              - std::tolower(static_cast<unsigned char>(y[i]));
        if (d != 0) return d;
    }
    return static_cast<int>(x.size()) - static_cast<int>(y.size());
}

inline std::vector<std::string *> *lines(std::string *s) {
    auto result = new std::vector<std::string *>();
    if (!s) return result;
    std::size_t pos = 0;
    while (pos <= s->size()) {
        std::size_t end = s->find('\n', pos);
        if (end == std::string::npos) end = s->size();
        std::size_t len = end - pos;
        if (len > 0 && (*s)[end - 1] == '\r') --len;
        result->push_back(new std::string(*s, pos, len));
        if (end == s->size()) break;
        pos = end + 1;
    }
    return result;
}

namespace detail {
inline const char *arg(std::string *s) { return s ? s->c_str() : ""; }
inline const char *arg(const std::string *s) { return s ? s->c_str() : ""; }
inline const char *arg(const std::string &s) { return s.c_str(); }
template <typename T> T arg(T value) { return value; }

inline std::string toString(std::string *s) { return s ? *s : std::string(); }
inline std::string toString(const std::string *s) { return s ? *s : std::string(); }
inline std::string toString(const std::string &s) { return s; }
inline std::string toString(const char *s) { return s ? std::string(s) : std::string(); }
inline std::string toString(char c) { return std::string(1, c); }
inline std::string toString(wchar_t c) { return std::string(1, static_cast<char>(c)); }
template <typename T> std::string javaFloating(T v) {
    char buf[64];
    auto res = std::to_chars(buf, buf + sizeof(buf), v);
    if (res.ec != std::errc()) return std::to_string(v);
    std::string s(buf, res.ptr);
    if (s.find_first_of(".eE") == std::string::npos) s += ".0";
    return s;
}
inline std::string toString(double v) { return javaFloating(v); }
inline std::string toString(float v) { return javaFloating(v); }
inline std::string toString(bool b) { return b ? "true" : "false"; }
inline std::string toString(std::nullptr_t) { return "null"; }
template <typename T> std::string toString(T *p) {
    return p ? *p->toString() : std::string("null");
}
template <typename T> std::string toString(T value) { return std::to_string(value); }
} //namespace detail

template <typename T> std::string *toStringPtr(T v) {
    return new std::string(detail::toString(v));
}

//java.lang.StringBuilder / StringBuffer
class StringBuilder {
    std::string value;
public:
    StringBuilder() = default;
    explicit StringBuilder(const std::string &s) : value(s) {}
    template <typename T> StringBuilder *append(T v) {
        value += detail::toString(v);
        return this;
    }
    StringBuilder *append(const std::string &s) { value += s; return this; }
    StringBuilder *insert(int pos, const std::string &s) {
        value.insert(static_cast<std::size_t>(pos), s); return this;
    }
    StringBuilder *remove(int start, int end) {
        value.erase(start, end - start); return this;
    }
    StringBuilder *reverse() {
        std::reverse(value.begin(), value.end()); return this;
    }
    int length() { return static_cast<int>(value.size()); }
    std::string *toString() { return new std::string(value); }
};

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

//the "+" operator on Java strings
template <typename... Args>
std::string *concat(Args... args) {
    std::string result;
    using expander = int[];
    (void)expander{0, (result += detail::toString(args), 0)...};
    return new std::string(result);
}

//String.format(String,Object[])
template <typename... Args>
//Java's %n is a platform newline, not C's write-count directive
inline std::string *javaFormat(std::string *fmt) {
    if (fmt == nullptr) return new std::string();
    std::string out;
    for (std::size_t i = 0; i < fmt->size(); ++i) {
        if ((*fmt)[i] == '%' && i + 1 < fmt->size() && (*fmt)[i + 1] == 'n') {
            out += '\n';
            ++i;
        }
        else {
            out += (*fmt)[i];
        }
    }
    return new std::string(out);
}

template <typename... Args>
std::string *format(std::string *fmt, Args... args) {
    if (fmt == nullptr) return new std::string();
    std::string *pattern = javaFormat(fmt);
    int size = std::snprintf(nullptr, 0, pattern->c_str(), detail::arg(args)...) + 1;
    if (size <= 0) throw std::runtime_error("CppHelper::format failed");
    std::unique_ptr<char[]> buf(new char[size]);
    std::snprintf(buf.get(), size, pattern->c_str(), detail::arg(args)...);
    return new std::string(buf.get(), buf.get() + size - 1);
}

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

//String.getBytes(String): encoding is accepted but not converted
inline bool startsWith(std::string *s, std::string *prefix) {
    if (s == nullptr || prefix == nullptr || prefix->size() > s->size()) return false;
    return s->compare(0, prefix->size(), *prefix) == 0;
}

inline bool endsWith(std::string *s, std::string *suffix) {
    if (s == nullptr || suffix == nullptr || suffix->size() > s->size()) return false;
    return s->compare(s->size() - suffix->size(), suffix->size(), *suffix) == 0;
}

inline std::string *encode(std::string *s, std::string *encoding) {
    (void)encoding;
    return new std::string(s ? *s : std::string());
}

inline std::string *replace(std::string *s, std::string *from, std::string *to) {
    if (s == nullptr) return new std::string();
    if (from == nullptr || to == nullptr || from->empty()) return new std::string(*s);
    std::string result = *s;
    size_t pos = 0;
    while ((pos = result.find(*from, pos)) != std::string::npos) {
        result.replace(pos, from->size(), *to);
        pos += to->size();
    }
    return new std::string(result);
}

//String.split(String)
inline std::vector<std::string *> *split(std::string *s, std::string *sep) {
    auto result = new std::vector<std::string *>();
    if (s == nullptr) return result;
    if (sep == nullptr || sep->empty()) {
        result->push_back(new std::string(*s));
        return result;
    }
    size_t start = 0;
    while (true) {
        size_t pos = s->find(*sep, start);
        if (pos == std::string::npos) {
            result->push_back(new std::string(s->substr(start)));
            break;
        }
        result->push_back(new std::string(s->substr(start, pos - start)));
        start = pos + sep->size();
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

//java.io.File / java.nio.file.Files -> std::filesystem
inline std::filesystem::path *fsPath(std::string *s) {
    return new std::filesystem::path(s ? *s : std::string());
}
inline std::filesystem::path *fsPathPair(std::string *a, std::string *b) {
    std::filesystem::path p(a ? *a : std::string());
    if (b) p /= *b;
    return new std::filesystem::path(p);
}
inline std::string *pathString(const std::filesystem::path &p) {
    return new std::string(p.string());
}
inline std::string *fsPathString(std::filesystem::path *p) {
    return p ? pathString(*p) : new std::string();
}
inline std::string *fsFileName(std::filesystem::path *p) {
    return p ? pathString(p->filename()) : new std::string();
}
inline std::string *fsParent(std::filesystem::path *p) {
    return p ? pathString(p->parent_path()) : new std::string();
}
inline std::string *fsAbsolute(std::filesystem::path *p) {
    if (!p) return new std::string();
    std::error_code ec;
    return pathString(std::filesystem::absolute(*p, ec));
}
inline bool fsExists(std::filesystem::path *p) {
    return p && std::filesystem::exists(*p);
}
inline bool fsIsDirectory(std::filesystem::path *p) {
    return p && std::filesystem::is_directory(*p);
}
inline bool fsIsRegularFile(std::filesystem::path *p) {
    return p && std::filesystem::is_regular_file(*p);
}
inline bool fsCreateDir(std::filesystem::path *p) {
    if (!p) return false;
    std::error_code ec;
    return std::filesystem::create_directories(*p, ec);
}
inline bool fsCreateFile(std::filesystem::path *p) {
    if (!p) return false;
    std::ofstream f(*p);
    return f.good();
}
inline bool fsRemove(std::filesystem::path *p) {
    if (!p) return false;
    std::error_code ec;
    return std::filesystem::remove(*p, ec);
}
inline bool fsRename(std::filesystem::path *from, std::filesystem::path *to) {
    if (!from || !to) return false;
    std::error_code ec;
    std::filesystem::rename(*from, *to, ec);
    return !ec;
}
inline long fsFileSize(std::filesystem::path *p) {
    if (!p) return 0;
    std::error_code ec;
    return static_cast<long>(std::filesystem::file_size(*p, ec));
}
inline std::vector<std::filesystem::path *> *fsList(std::filesystem::path *p) {
    auto result = new std::vector<std::filesystem::path *>();
    if (!p) return result;
    std::error_code ec;
    for (const auto &entry : std::filesystem::directory_iterator(*p, ec)) {
        result->push_back(new std::filesystem::path(entry.path()));
    }
    return result;
}
inline std::string *fsReadString(std::filesystem::path *p) {
    if (!p) return new std::string();
    std::ifstream f(*p, std::ios::binary);
    return new std::string(std::istreambuf_iterator<char>(f), std::istreambuf_iterator<char>());
}
inline bool fsWriteString(std::filesystem::path *p, std::string *s) {
    if (!p) return false;
    std::ofstream f(*p, std::ios::binary);
    if (s) f.write(s->data(), static_cast<std::streamsize>(s->size()));
    return f.good();
}
inline std::vector<std::string *> *fsReadAllLines(std::filesystem::path *p) {
    auto result = new std::vector<std::string *>();
    if (!p) return result;
    std::ifstream f(*p);
    std::string line;
    while (std::getline(f, line)) {
        if (!line.empty() && line.back() == '\r') line.pop_back();
        result->push_back(new std::string(line));
    }
    return result;
}

//java.lang.System.out / System.err
class PrintStream {
    std::ostream &stream;
public:
    explicit PrintStream(std::ostream &s) : stream(s) {}

    template <typename T> PrintStream *print(T value) {
        stream << detail::toString(value);
        return this;
    }
    template <typename T> PrintStream *println(T value) {
        stream << detail::toString(value) << '\n';
        return this;
    }
    PrintStream *println() { stream << '\n'; return this; }
    void flush() { stream.flush(); }

    template <typename... Args>
    PrintStream *printf(std::string *fmt, Args... args) {
        stream << *CppHelper::format(fmt, args...);
        return this;
    }
    template <typename... Args>
    PrintStream *format(std::string *fmt, Args... args) {
        stream << *CppHelper::format(fmt, args...);
        return this;
    }
};

inline PrintStream *javaOut() {
    static PrintStream out(std::cout);
    return &out;
}
inline PrintStream *javaErr() {
    static PrintStream err(std::cerr);
    return &err;
}

} //namespace CppHelper

//older generated code refers to the array helpers as arrays::*
namespace arrays = CppHelper;

namespace j2cpp {

//Execute try and finally with Java semantics: finally always runs, and a value
//returned from finally overrides the try/catch result or any pending exception.
//An exception thrown from finally replaces any pending exception.
//
//The generator passes the try/catch part and the finally part as lambdas that
//return std::optional<R> (with R the method's return type). A Java `return X;`
//becomes `return X;` inside the lambda (implicitly wrapped in the optional);
//falling off the end becomes `return std::nullopt;`. An empty optional from the
//try part means the try block completed normally without returning, so execution
//continues after the try/finally.
template <typename TryCode, typename FinallyCode>
auto try_finally_value(TryCode&& tryCode, FinallyCode&& finallyCode) -> decltype(tryCode()) {
    decltype(tryCode()) result;
    try {
        result = tryCode();
    }
    catch (...) {
        auto fin = finallyCode();
        if (fin.has_value()) return fin;
        throw;
    }
    auto fin = finallyCode();
    if (fin.has_value()) return fin;
    return result;
}

//void version: the try lambda returns true if it executed a Java `return;`.
template <typename TryCode, typename FinallyCode>
bool try_finally_void(TryCode&& tryCode, FinallyCode&& finallyCode) {
    bool returned = false;
    try {
        returned = tryCode();
    }
    catch (...) {
        finallyCode();
        throw;
    }
    finallyCode();
    return returned;
}

} //namespace j2cpp
