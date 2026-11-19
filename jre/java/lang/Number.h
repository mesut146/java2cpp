#pragma once

#include "jre/java/lang/Object.h"

namespace java{
namespace lang{

class Number : public Object {
public:
    virtual int intValue() { return 0; }
    virtual long longValue() { return 0; }
    virtual float floatValue() { return 0.0f; }
    virtual double doubleValue() { return 0.0; }
    virtual short shortValue() { return 0; }
    virtual char byteValue() { return 0; }
};

}//namespace lang
}//namespace java
