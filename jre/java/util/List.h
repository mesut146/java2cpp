#pragma once

#include <vector>
#include "common.h"
#include "jre/java/lang/Object.h"

namespace java{
namespace util{

template <typename E>
class List : public java::lang::Object {
public:
    virtual bool add(E value) = 0;
    virtual void add(int index, E value) = 0;
    virtual int size() = 0;
    virtual bool isEmpty() = 0;
    virtual E get(int index) = 0;
    virtual E set(int index, E value) = 0;
    virtual E remove(int index) = 0;
    virtual bool remove(java::lang::Object* value) = 0;
    virtual int indexOf(java::lang::Object* value) = 0;
    virtual int lastIndexOf(java::lang::Object* value) = 0;
    virtual bool contains(java::lang::Object* value) = 0;
    virtual std::vector<E>* toArray(std::vector<E>* target) = 0;
    virtual void clear() = 0;
};

}//namespace util
}//namespace java
