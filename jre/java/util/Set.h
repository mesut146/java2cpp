#pragma once

#include "common.h"
#include "jre/java/lang/Object.h"

namespace java{
namespace util{

template <typename E>
class Set : public java::lang::Object {
public:
    virtual bool add(E value) = 0;
    virtual bool remove(java::lang::Object* value) = 0;
    virtual bool contains(java::lang::Object* value) = 0;
    virtual int size() = 0;
    virtual bool isEmpty() = 0;
    virtual void clear() = 0;
};

}//namespace util
}//namespace java
