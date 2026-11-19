#pragma once

#include "jre/java/lang/Throwable.h"

namespace java{
namespace lang{

class Error : public Throwable {
public:
    Error() : Throwable() {}
    explicit Error(String* message) : Throwable(message) {}
    Error(String* message, Throwable* cause) : Throwable(message, cause) {}
    explicit Error(Throwable* cause) : Throwable(cause) {}
};

}//namespace lang
}//namespace java
