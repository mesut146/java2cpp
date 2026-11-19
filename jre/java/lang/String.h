#pragma once

#include <string>
#include <vector>
#include <cwchar>
#include <cwctype>
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
    std::wstring value_;
public:
    String() {}
    String(String* other) { if (other) value_ = other->value_; }
    explicit String(std::vector<wchar_t>* chars) {
        if (chars) value_.assign(chars->begin(), chars->end());
    }
    explicit String(std::vector<char>* bytes) {
        if (bytes) for (char c : *bytes) value_.push_back(static_cast<wchar_t>(static_cast<unsigned char>(c)));
    }
    String(std::vector<char>* bytes, const std::string* enc) { (void)enc; if (bytes) for (char c : *bytes) value_.push_back(static_cast<wchar_t>(static_cast<unsigned char>(c))); }
    explicit String(const std::string* s) {
        if (s) for (char c : *s) value_.push_back(static_cast<wchar_t>(static_cast<unsigned char>(c)));
    }

    int length() { return static_cast<int>(value_.size()); }
    bool isEmpty() { return value_.empty(); }
    wchar_t charAt(int i) { return i >= 0 && i < static_cast<int>(value_.size()) ? value_[i] : 0; }

    std::string* utf8() {
        std::string out;
        for (wchar_t c : value_) out.push_back(static_cast<char>(c));
        return new std::string(out);
    }
    String* toString() override { return this; }

    bool equals(Object* o) override {
        String* other = dynamic_cast<String*>(o);
        return other && other->value_ == value_;
    }
    bool equals(std::string* s) {
        return s && utf8()->compare(*s) == 0;
    }
    int hashCode() override {
        int h = 0;
        for (wchar_t c : value_) h = 31 * h + static_cast<int>(c);
        return h;
    }
    int compareTo(String* other) {
        if (!other) return 1;
        return value_ == other->value_ ? 0 : (value_ < other->value_ ? -1 : 1);
    }
    int compareToIgnoreCase(String* other) {
        if (!other) return 1;
        std::wstring a = value_, b = other->value_;
        for (auto& c : a) c = static_cast<wchar_t>(std::towlower(c));
        for (auto& c : b) c = static_cast<wchar_t>(std::towlower(c));
        return a == b ? 0 : (a < b ? -1 : 1);
    }

    String* substring(int begin) { return substring(begin, static_cast<int>(value_.size())); }
    String* substring(int begin, int end) {
        if (begin < 0) begin = 0;
        if (end > static_cast<int>(value_.size())) end = static_cast<int>(value_.size());
        if (end < begin) end = begin;
        auto* chars = new std::vector<wchar_t>(value_.begin() + begin, value_.begin() + end);
        return new String(chars);
    }
    String* concat(String* other) {
        std::wstring joined = value_;
        if (other) joined += other->value_;
        auto* chars = new std::vector<wchar_t>(joined.begin(), joined.end());
        return new String(chars);
    }
    bool contains(String* other) { return other && value_.find(other->value_) != std::wstring::npos; }
    bool startsWith(String* prefix) {
        return prefix && value_.size() >= prefix->value_.size()
                && value_.compare(0, prefix->value_.size(), prefix->value_) == 0;
    }
    bool endsWith(String* suffix) {
        return suffix && value_.size() >= suffix->value_.size()
                && value_.compare(value_.size() - suffix->value_.size(), suffix->value_.size(), suffix->value_) == 0;
    }
    int indexOf(wchar_t c) {
        auto pos = value_.find(c);
        return pos == std::wstring::npos ? -1 : static_cast<int>(pos);
    }
    int indexOf(String* s) {
        if (!s) return -1;
        auto pos = value_.find(s->value_);
        return pos == std::wstring::npos ? -1 : static_cast<int>(pos);
    }
    int lastIndexOf(wchar_t c) {
        auto pos = value_.rfind(c);
        return pos == std::wstring::npos ? -1 : static_cast<int>(pos);
    }
    int lastIndexOf(String* s) {
        if (!s) return -1;
        auto pos = value_.rfind(s->value_);
        return pos == std::wstring::npos ? -1 : static_cast<int>(pos);
    }
    std::vector<wchar_t>* toCharArray() { return new std::vector<wchar_t>(value_.begin(), value_.end()); }

    static String* valueOf(int v) { return new String(new std::string(std::to_string(v))); }
    static String* valueOf(long v) { return new String(new std::string(std::to_string(v))); }
    static String* valueOf(bool b) { return new String(new std::string(b ? "true" : "false")); }
    static String* valueOf(Object* o) {
        return o ? new String(o->toString()) : new String(new std::string("null"));
    }
};

inline String* Object::toString() {
    return new String(new std::string("java.lang.Object"));
}

}//namespace lang
}//namespace java
