#pragma once

#include "common.h"
#include "jre/java/lang/Object.h"

namespace java{
namespace util{

template <typename E>
class Collection : public java::lang::Object {
public:
    virtual int size() = 0;
    virtual bool isEmpty() = 0;
    virtual E get(int index) = 0;
    virtual bool contains(java::lang::Object* value) = 0;
};

}//namespace util
}//namespace java
