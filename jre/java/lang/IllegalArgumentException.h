#pragma once

#include "jre/java/lang/RuntimeException.h"

namespace java{
namespace lang{

class IllegalArgumentException : public RuntimeException {
public:
    IllegalArgumentException() : RuntimeException() {}
    explicit IllegalArgumentException(String* message) : RuntimeException(message) {}
    IllegalArgumentException(String* message, Throwable* cause) : RuntimeException(message, cause) {}
    explicit IllegalArgumentException(Throwable* cause) : RuntimeException(cause) {}
};

}//namespace lang
}//namespace java
