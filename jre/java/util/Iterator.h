#pragma once

#include "common.h"
#include "jre/java/lang/Object.h"

namespace java{
namespace util{

class Iterator : public java::lang::Object {
public:
    virtual bool hasNext() = 0;
    virtual java::lang::Object* next() = 0;
};

}//namespace util
}//namespace java
