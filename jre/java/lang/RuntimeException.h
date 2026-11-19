#pragma once

#include "jre/java/lang/Exception.h"

namespace java{
namespace lang{

class RuntimeException : public Exception {
public:
    RuntimeException() : Exception() {}
    explicit RuntimeException(String* message) : Exception(message) {}
    RuntimeException(String* message, Throwable* cause) : Exception(message, cause) {}
    explicit RuntimeException(Throwable* cause) : Exception(cause) {}
};

}//namespace lang
}//namespace java
