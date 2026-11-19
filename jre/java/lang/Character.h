#pragma once

#include <string>
#include "jre/java/lang/Object.h"
#include "jre/java/lang/String.h"

namespace java{
namespace lang{

class Character : public Object {
    wchar_t value_;
public:
    static wchar_t MAX_VALUE;
    static wchar_t MIN_VALUE;
    explicit Character(wchar_t v) : value_(v) {}
    wchar_t charValue() { return value_; }
    bool equals(Object* o) override {
        Character* other = dynamic_cast<Character*>(o);
        return other && other->value_ == value_;
    }
    int hashCode() override { return static_cast<int>(value_); }
    String* toString() override { return new String(new std::string(1, static_cast<char>(value_))); }
    static Character* valueOf(wchar_t v) { return new Character(v); }
};

inline wchar_t Character::MAX_VALUE = 65535;
inline wchar_t Character::MIN_VALUE = 0;

}//namespace lang
}//namespace java
