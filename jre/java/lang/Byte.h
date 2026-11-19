#pragma once

#include <string>
#include "jre/java/lang/Number.h"
#include "jre/java/lang/String.h"

namespace java{
namespace lang{

class Byte : public Number {
    char value_;
public:
    explicit Byte(char v) : value_(v) {}
    char byteValue() override { return value_; }
    int intValue() override { return static_cast<int>(value_); }
    String* toString() override { return new String(new std::string(std::to_string(static_cast<int>(value_)))); }
    bool equals(Object* o) override {
        Byte* other = dynamic_cast<Byte*>(o);
        return other && other->value_ == value_;
    }
    int hashCode() override { return static_cast<int>(value_); }
    static Byte* valueOf(char v) { return new Byte(v); }
};

}//namespace lang
}//namespace java
