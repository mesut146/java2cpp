#pragma once

#include "common.h"
#include "jre/java/lang/Object.h"
#include "jre/java/util/Iterator.h"

namespace java{
namespace util{

template <typename E>
class Set : public Iterator {
    size_t cursor_ = 0;
public:
    virtual bool add(E value) = 0;
    virtual bool remove(java::lang::Object* value) = 0;
    virtual bool contains(java::lang::Object* value) = 0;
    virtual E get(int index) = 0;
    virtual int size() = 0;
    virtual bool isEmpty() = 0;
    virtual void clear() = 0;

    bool hasNext() override { return cursor_ < static_cast<size_t>(size()); }
    java::lang::Object* next() override {
        return hasNext() ? static_cast<java::lang::Object*>(get(static_cast<int>(cursor_++))) : nullptr;
    }
};

}//namespace util
}//namespace java
