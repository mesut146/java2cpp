#pragma once

#include "jre/java/lang/RuntimeException.h"

namespace java{
namespace lang{

class IllegalStateException : public RuntimeException {
public:
    IllegalStateException() : RuntimeException() {}
    explicit IllegalStateException(String* message) : RuntimeException(message) {}
    IllegalStateException(String* message, Throwable* cause) : RuntimeException(message, cause) {}
    explicit IllegalStateException(Throwable* cause) : RuntimeException(cause) {}
};

}//namespace lang
}//namespace java
