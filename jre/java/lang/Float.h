#pragma once

#include <string>
#include "jre/java/lang/Number.h"
#include "jre/java/lang/String.h"

namespace java{
namespace lang{

class Float : public Number {
    float value_;
public:
    explicit Float(float v) : value_(v) {}
    float floatValue() override { return value_; }
    int intValue() override { return static_cast<int>(value_); }
    String* toString() override { return new String(new std::string(std::to_string(value_))); }
    bool equals(Object* o) override {
        Float* other = dynamic_cast<Float*>(o);
        return other && other->value_ == value_;
    }
    int hashCode() override { return static_cast<int>(value_); }
    static Float* valueOf(float v) { return new Float(v); }
};

}//namespace lang
}//namespace java
