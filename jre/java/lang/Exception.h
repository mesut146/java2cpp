#pragma once

#include "jre/java/lang/Throwable.h"

namespace java{
namespace lang{

class Exception : public Throwable {
public:
    Exception() : Throwable() {}
    explicit Exception(String* message) : Throwable(message) {}
    Exception(String* message, Throwable* cause) : Throwable(message, cause) {}
    explicit Exception(Throwable* cause) : Throwable(cause) {}
};

}//namespace lang
}//namespace java
