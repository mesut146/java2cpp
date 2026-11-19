#pragma once

#include <string>
#include "jre/java/lang/Number.h"
#include "jre/java/lang/String.h"

namespace java{
namespace lang{

class Double : public Number {
    double value_;
public:
    explicit Double(double v) : value_(v) {}
    double doubleValue() override { return value_; }
    int intValue() override { return static_cast<int>(value_); }
    String* toString() override { return new String(new std::string(std::to_string(value_))); }
    bool equals(Object* o) override {
        Double* other = dynamic_cast<Double*>(o);
        return other && other->value_ == value_;
    }
    int hashCode() override { return static_cast<int>(value_); }
    static Double* valueOf(double v) { return new Double(v); }
};

}//namespace lang
}//namespace java
