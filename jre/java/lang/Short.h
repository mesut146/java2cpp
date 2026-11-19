#pragma once

#include <string>
#include "jre/java/lang/Number.h"
#include "jre/java/lang/String.h"

namespace java{
namespace lang{

class Short : public Number {
    short value_;
public:
    explicit Short(short v) : value_(v) {}
    short shortValue() override { return value_; }
    int intValue() override { return static_cast<int>(value_); }
    String* toString() override { return new String(new std::string(std::to_string(value_))); }
    bool equals(Object* o) override {
        Short* other = dynamic_cast<Short*>(o);
        return other && other->value_ == value_;
    }
    int hashCode() override { return static_cast<int>(value_); }
    static Short* valueOf(short v) { return new Short(v); }
};

}//namespace lang
}//namespace java
