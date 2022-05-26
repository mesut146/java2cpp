#pragma once

#include "java/lang/RuntimeException.h"

namespace java {
    namespace lang {

        class IllegalArgumentException : public RuntimeException {
        public:
            explicit IllegalArgumentException(std::string *msg);

            explicit IllegalArgumentException(std::string &&msg);

            explicit IllegalArgumentException(const String &s);

            IllegalArgumentException();
        }; //class IllegalArgumentException

    } //namespace java
} //namespace lang
