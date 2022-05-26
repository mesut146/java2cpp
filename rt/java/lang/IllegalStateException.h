#pragma once

#include "java/lang/RuntimeException.h"

namespace java {
    namespace lang {

        class IllegalStateException : public RuntimeException {
        public:
            explicit IllegalStateException(std::string *s);
            explicit IllegalStateException(std::string &&s);
        }; //class IllegalStateException

    } //namespace java
} //namespace lang
