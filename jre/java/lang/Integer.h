#pragma once

#include <string>
#include <vector>
#include <cstdlib>
#include "jre/java/lang/Number.h"
#include "jre/java/lang/String.h"

namespace java{
namespace lang{

class Integer : public Number {
    int value_;
public:
    explicit Integer(int v) : value_(v) {}
    explicit Integer(std::string* s) : value_(s ? std::atoi(s->c_str()) : 0) {}

    int intValue() override { return value_; }
    long longValue() override { return value_; }
    float floatValue() override { return static_cast<float>(value_); }
    double doubleValue() override { return static_cast<double>(value_); }
    short shortValue() override { return static_cast<short>(value_); }
    char byteValue() override { return static_cast<char>(value_); }

    String* toString() override { return new String(new std::string(std::to_string(value_))); }
    bool equals(Object* o) override {
        Integer* other = dynamic_cast<Integer*>(o);
        return other && other->value_ == value_;
    }
    int hashCode() override { return value_; }

    static Integer* valueOf(int v) { return new Integer(v); }
    static int parseInt(std::string* s) { return s ? std::atoi(s->c_str()) : 0; }
    static std::string* toString(int v) { return new std::string(std::to_string(v)); }
    static std::string* toHexString(int v) {
        char buf[32];
        std::snprintf(buf, sizeof(buf), "%x", static_cast<unsigned int>(v));
        return new std::string(buf);
    }
    static std::string* toBinaryString(int v) {
        std::string out;
        unsigned int n = static_cast<unsigned int>(v);
        for (int i = 31; i >= 0; --i) out += (n & (1u << i)) ? '1' : '0';
        return new std::string(out);
    }
    static int max(int a, int b) { return a > b ? a : b; }
    static int min(int a, int b) { return a < b ? a : b; }
};

}//namespace lang
}//namespace java
