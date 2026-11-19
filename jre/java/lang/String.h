#pragma once

#include <algorithm>
#include <cstdint>
#include <cwctype>
#include <string>
#include <vector>
#include "common.h"
#include "lib/java/io/Serializable.h"
#include "lib/java/lang/Comparable.h"
#include "lib/java/lang/CharSequence.h"
#include "lib/java/lang/constant/Constable.h"
#include "lib/java/lang/constant/ConstantDesc.h"
#include "jre/java/lang/Object.h"

namespace java{
namespace lang{

class String : public Object, public java::io::Serializable,
        public Comparable<String*>, public CharSequence,
        public java::lang::constant::Constable,
        public java::lang::constant::ConstantDesc {
    std::u16string value_;

    static void appendCodePoint(std::u16string& target, uint32_t cp) {
        if (cp <= 0xFFFF) {
            target.push_back(static_cast<char16_t>(cp));
        }
        else {
            cp -= 0x10000;
            target.push_back(static_cast<char16_t>(0xD800 + (cp >> 10)));
            target.push_back(static_cast<char16_t>(0xDC00 + (cp & 0x3FF)));
        }
    }

    static std::u16string fromUtf8(const std::string& bytes) {
        std::u16string out;
        size_t i = 0;
        while (i < bytes.size()) {
            unsigned char c = static_cast<unsigned char>(bytes[i]);
            uint32_t cp;
            int extra;
            if (c < 0x80) { cp = c; extra = 0; }
            else if ((c & 0xE0) == 0xC0) { cp = c & 0x1F; extra = 1; }
            else if ((c & 0xF0) == 0xE0) { cp = c & 0x0F; extra = 2; }
            else if ((c & 0xF8) == 0xF0) { cp = c & 0x07; extra = 3; }
            else { ++i; continue; }
            for (int k = 0; k < extra && i + 1 < bytes.size(); ++k) {
                cp = (cp << 6) | (static_cast<unsigned char>(bytes[++i]) & 0x3F);
            }
            ++i;
            if (cp <= 0xFFFF) out.push_back(static_cast<char16_t>(cp));
            else {
                cp -= 0x10000;
                out.push_back(static_cast<char16_t>(0xD800 + (cp >> 10)));
                out.push_back(static_cast<char16_t>(0xDC00 + (cp & 0x3FF)));
            }
        }
        return out;
    }

public:
    String() {}
    String(String* other) { if (other) value_ = other->value_; }
    explicit String(std::vector<wchar_t>* chars) {
        if (chars) for (wchar_t c : *chars) appendCodePoint(value_, static_cast<uint32_t>(c));
    }
    explicit String(std::vector<char>* bytes) {
        if (bytes) value_ = fromUtf8(std::string(bytes->begin(), bytes->end()));
    }
    String(std::vector<char>* bytes, const std::string* enc) { (void)enc; if (bytes) value_ = fromUtf8(std::string(bytes->begin(), bytes->end())); }
    explicit String(const std::string* s) { if (s) value_ = fromUtf8(*s); }

    int length() { return static_cast<int>(value_.size()); }
    bool isEmpty() { return value_.empty(); }
    wchar_t charAt(int i) {
        return (i >= 0 && i < static_cast<int>(value_.size()))
                ? static_cast<wchar_t>(value_[i]) : 0;
    }

    std::string* utf8() {
        std::string out;
        for (size_t i = 0; i < value_.size(); ++i) {
            uint32_t cp = value_[i];
            if (cp >= 0xD800 && cp <= 0xDBFF && i + 1 < value_.size()
                    && value_[i + 1] >= 0xDC00 && value_[i + 1] <= 0xDFFF) {
                cp = 0x10000 + ((cp - 0xD800) << 10) + (value_[++i] - 0xDC00);
            }
            if (cp < 0x80) out.push_back(static_cast<char>(cp));
            else if (cp < 0x800) {
                out.push_back(static_cast<char>(0xC0 | (cp >> 6)));
                out.push_back(static_cast<char>(0x80 | (cp & 0x3F)));
            }
            else if (cp < 0x10000) {
                out.push_back(static_cast<char>(0xE0 | (cp >> 12)));
                out.push_back(static_cast<char>(0x80 | ((cp >> 6) & 0x3F)));
                out.push_back(static_cast<char>(0x80 | (cp & 0x3F)));
            }
            else {
                out.push_back(static_cast<char>(0xF0 | (cp >> 18)));
                out.push_back(static_cast<char>(0x80 | ((cp >> 12) & 0x3F)));
                out.push_back(static_cast<char>(0x80 | ((cp >> 6) & 0x3F)));
                out.push_back(static_cast<char>(0x80 | (cp & 0x3F)));
            }
        }
        return new std::string(out);
    }
    String* toString() override { return this; }

    bool equals(Object* o) override {
        String* other = dynamic_cast<String*>(o);
        return other && other->value_ == value_;
    }
    bool equals(std::string* s) { return s && *utf8() == *s; }
    bool equalsIgnoreCase(String* other) {
        return other && lower(value_) == lower(other->value_);
    }
    int hashCode() override {
        uint32_t h = 0;
        for (char16_t c : value_) h = 31u * h + static_cast<uint16_t>(c);
        return static_cast<int32_t>(h);
    }
    int compareTo(String* other) {
        if (!other) return 1;
        size_t n = std::min(value_.size(), other->value_.size());
        for (size_t i = 0; i < n; ++i) {
            if (value_[i] != other->value_[i]) {
                return static_cast<int>(value_[i]) - static_cast<int>(other->value_[i]);
            }
        }
        return static_cast<int>(value_.size()) - static_cast<int>(other->value_.size());
    }
    int compareToIgnoreCase(String* other) {
        if (!other) return 1;
        std::u16string a = lower(value_), b = lower(other->value_);
        size_t n = std::min(a.size(), b.size());
        for (size_t i = 0; i < n; ++i) {
            if (a[i] != b[i]) return static_cast<int>(a[i]) - static_cast<int>(b[i]);
        }
        return static_cast<int>(a.size()) - static_cast<int>(b.size());
    }

    String* substring(int begin) { return substring(begin, static_cast<int>(value_.size())); }
    String* substring(int begin, int end) {
        if (begin < 0) begin = 0;
        if (end > static_cast<int>(value_.size())) end = static_cast<int>(value_.size());
        if (end < begin) end = begin;
        return fromUnits(value_.substr(begin, end - begin));
    }
    String* concat(String* other) {
        std::u16string joined = value_;
        if (other) joined += other->value_;
        return fromUnits(joined);
    }
    bool contains(String* other) {
        return other && value_.find(other->value_) != std::u16string::npos;
    }
    bool startsWith(String* prefix) {
        return prefix && value_.compare(0, prefix->value_.size(), prefix->value_) == 0;
    }
    bool endsWith(String* suffix) {
        return suffix && value_.size() >= suffix->value_.size()
                && value_.compare(value_.size() - suffix->value_.size(),
                                  suffix->value_.size(), suffix->value_) == 0;
    }
    int indexOf(wchar_t c) {
        auto pos = value_.find(static_cast<char16_t>(c));
        return pos == std::u16string::npos ? -1 : static_cast<int>(pos);
    }
    int indexOf(String* s) {
        if (!s) return -1;
        auto pos = value_.find(s->value_);
        return pos == std::u16string::npos ? -1 : static_cast<int>(pos);
    }
    int lastIndexOf(wchar_t c) {
        auto pos = value_.rfind(static_cast<char16_t>(c));
        return pos == std::u16string::npos ? -1 : static_cast<int>(pos);
    }
    int lastIndexOf(String* s) {
        if (!s) return -1;
        auto pos = value_.rfind(s->value_);
        return pos == std::u16string::npos ? -1 : static_cast<int>(pos);
    }
    std::vector<wchar_t>* toCharArray() {
        auto* out = new std::vector<wchar_t>();
        for (char16_t c : value_) out->push_back(static_cast<wchar_t>(c));
        return out;
    }

    String* replace(wchar_t from, wchar_t to) {
        std::u16string out = value_;
        for (char16_t& c : out) if (c == from) c = static_cast<char16_t>(to);
        return fromUnits(out);
    }
    String* replace(String* from, String* to) {
        if (!from || from->value_.empty()) return this;
        std::u16string out;
        size_t pos = 0;
        while (true) {
            size_t next = value_.find(from->value_, pos);
            if (next == std::u16string::npos) { out += value_.substr(pos); break; }
            out += value_.substr(pos, next - pos);
            out += to ? to->value_ : std::u16string();
            pos = next + from->value_.size();
        }
        return fromUnits(out);
    }
    std::vector<String*>* split(String* sep, int limit) {
        auto* result = new std::vector<String*>();
        if (!sep || sep->value_.empty()) { result->push_back(this); return result; }
        size_t start = 0;
        while (true) {
            size_t pos = (limit > 0 && result->size() == static_cast<size_t>(limit) - 1)
                    ? std::u16string::npos : value_.find(sep->value_, start);
            if (pos == std::u16string::npos) {
                std::u16string last = value_.substr(start);
                if (limit != 0 || !last.empty() || result->empty()) result->push_back(fromUnits(last));
                break;
            }
            result->push_back(fromUnits(value_.substr(start, pos - start)));
            start = pos + sep->value_.size();
        }
        return result;
    }
    std::vector<String*>* split(String* sep) { return split(sep, 0); }

    String* trim() {
        size_t b = 0, e = value_.size();
        while (b < e && value_[b] <= u' ') ++b;
        while (e > b && value_[e - 1] <= u' ') --e;
        return fromUnits(value_.substr(b, e - b));
    }
    String* strip() { return trim(); }
    bool isBlank() {
        for (char16_t c : value_) {
            if (c != u' ' && c != u'\t' && c != u'\n' && c != u'\r') return false;
        }
        return true;
    }
    String* toUpperCase() {
        std::u16string out = value_;
        for (char16_t& c : out) if (c >= u'a' && c <= u'z') c = static_cast<char16_t>(c - 32);
        return fromUnits(out);
    }
    String* toLowerCase() { return fromUnits(lower(value_)); }

    static String* join(String* delim, std::vector<CharSequence*>* parts) {
        std::u16string out;
        bool first = true;
        if (parts) for (CharSequence* part : *parts) {
            if (!first && delim) out += delim->value_;
            first = false;
            String* s = dynamic_cast<String*>(part);
            if (s) out += s->value_;
        }
        return fromUnits(out);
    }

    static String* valueOf(int v) { return new String(new std::string(std::to_string(v))); }
    static String* valueOf(long v) { return new String(new std::string(std::to_string(v))); }
    static String* valueOf(bool b) { return new String(new std::string(b ? "true" : "false")); }
    static String* valueOf(wchar_t c) {
        std::u16string s;
        uint32_t cp = static_cast<uint32_t>(c);
        if (cp <= 0xFFFF) s.push_back(static_cast<char16_t>(cp));
        else { cp -= 0x10000; s.push_back(static_cast<char16_t>(0xD800 + (cp >> 10))); s.push_back(static_cast<char16_t>(0xDC00 + (cp & 0x3FF))); }
        return fromUnits(s);
    }
    static String* valueOf(Object* o) {
        return o ? new String(o->toString()) : new String(new std::string("null"));
    }
    static String* format(String* pattern, std::vector<Object*>* args) {
        std::string out;
        std::string pat = pattern ? *pattern->utf8() : std::string();
        size_t index = 0;
        for (size_t i = 0; i < pat.size(); ++i) {
            if (pat[i] != '%' || i + 1 >= pat.size()) { out += pat[i]; continue; }
            char spec = pat[++i];
            if (spec == '%') { out += '%'; continue; }
            if (spec == 'n') { out += '\n'; continue; }
            std::string value;
            if (args && index < args->size()) {
                Object* arg = args->at(index++);
                String* s = dynamic_cast<String*>(arg);
                value = s ? *s->utf8() : (arg ? *arg->toString()->utf8() : "null");
            }
            else value = "null";
            if (spec == 's' || spec == 'd' || spec == 'f' || spec == 'c') out += value;
            else { out += '%'; out += spec; }
        }
        return new String(new std::string(out));
    }

private:
    static std::u16string lower(const std::u16string& s) {
        std::u16string out = s;
        for (char16_t& c : out) if (c >= u'A' && c <= u'Z') c = static_cast<char16_t>(c + 32);
        return out;
    }
    static String* fromUnits(const std::u16string& units) {
        String* result = new String();
        result->value_ = units;
        return result;
    }
};

inline String* Object::toString() {
    return new String(new std::string("java.lang.Object"));
}

}//namespace lang
}//namespace java
