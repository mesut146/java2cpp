#pragma once

#include "jre/java/lang/Object.h"
#include "jre/java/lang/String.h"

namespace java{
namespace util{

class Objects {
public:
    static bool equals(java::lang::Object* a, java::lang::Object* b) {
        if (a == b) return true;
        return a && a->equals(b);
    }
    static int hashCode(java::lang::Object* o) { return o ? o->hashCode() : 0; }
    static java::lang::String* toString(java::lang::Object* o) {
        return o ? o->toString() : new java::lang::String(new std::string("null"));
    }
    static bool isNull(java::lang::Object* o) { return o == nullptr; }
    static bool nonNull(java::lang::Object* o) { return o != nullptr; }
    static java::lang::Object* requireNonNull(java::lang::Object* o) { return o; }
};

}//namespace util
}//namespace java
