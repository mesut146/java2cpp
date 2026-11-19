#pragma once

#include <string>
#include "jre/java/lang/Object.h"
#include "jre/java/lang/String.h"

namespace java{
namespace lang{

class Boolean : public Object {
    bool value_;
public:
    explicit Boolean(bool v) : value_(v) {}
    explicit Boolean(std::string* s) : value_(s && *s == "true") {}

    bool booleanValue() { return value_; }
    String* toString() override { return new String(new std::string(value_ ? "true" : "false")); }
    bool equals(Object* o) override {
        Boolean* other = dynamic_cast<Boolean*>(o);
        return other && other->value_ == value_;
    }
    int hashCode() override { return value_ ? 1231 : 1237; }

    static Boolean* valueOf(bool v) { return new Boolean(v); }
    static bool parseBoolean(std::string* s) { return s && *s == "true"; }
    static std::string* toString(bool v) { return new std::string(v ? "true" : "false"); }
};

}//namespace lang
}//namespace java
