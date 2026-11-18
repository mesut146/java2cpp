#pragma once

//Java String / String.format / StringBuilder helpers used by the bundled mappers
#include <algorithm>
#include <cctype>
#include <charconv>
#include <cstdio>
#include <cstdlib>
#include <initializer_list>
#include <memory>
#include <stdexcept>
#include <string>
#include <vector>

namespace CppHelper {

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

} //namespace CppHelper
