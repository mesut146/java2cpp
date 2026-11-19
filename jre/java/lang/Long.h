#pragma once

#include <string>
#include "jre/java/lang/Number.h"
#include "jre/java/lang/String.h"

namespace java{
namespace lang{

class Long : public Number {
    long value_;
public:
    explicit Long(long v) : value_(v) {}
    long longValue() override { return value_; }
    int intValue() override { return static_cast<int>(value_); }
    String* toString() override { return new String(new std::string(std::to_string(value_))); }
    bool equals(Object* o) override {
        Long* other = dynamic_cast<Long*>(o);
        return other && other->value_ == value_;
    }
    int hashCode() override { return static_cast<int>(value_); }
    static Long* valueOf(long v) { return new Long(v); }
};

}//namespace lang
}//namespace java
