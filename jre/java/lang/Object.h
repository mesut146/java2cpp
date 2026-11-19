#pragma once

namespace java{
namespace lang{

class String;

class Object {
public:
    virtual ~Object() = default;
    virtual String* toString();
    virtual bool equals(Object* o) { return this == o; }
    virtual int hashCode() { return static_cast<int>(reinterpret_cast<unsigned long>(this) & 0x7fffffffu); }
};

}//namespace lang
}//namespace java
