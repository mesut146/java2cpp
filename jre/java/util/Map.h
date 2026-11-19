#pragma once

#include <vector>
#include "common.h"
#include "jre/java/lang/Object.h"

namespace java{
namespace util{

template <typename K, typename V>
class Map : public java::lang::Object {
public:
    virtual V put(K key, V value) = 0;
    virtual V get(java::lang::Object* key) = 0;
    virtual V getOrDefault(java::lang::Object* key, V fallback) = 0;
    virtual V putIfAbsent(K key, V value) = 0;
    virtual V remove(java::lang::Object* key) = 0;
    virtual bool remove(java::lang::Object* key, java::lang::Object* value) = 0;
    virtual bool containsKey(java::lang::Object* key) = 0;
    virtual bool containsValue(java::lang::Object* value) = 0;
    virtual int size() = 0;
    virtual bool isEmpty() = 0;
    virtual void clear() = 0;
};

}//namespace util
}//namespace java
