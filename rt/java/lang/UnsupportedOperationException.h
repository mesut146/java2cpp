#pragma once

#include "java/lang/RuntimeException.h"

namespace java {
    namespace lang {

        class UnsupportedOperationException : public RuntimeException {
        public:
            explicit UnsupportedOperationException(std::string *msg);

            explicit UnsupportedOperationException(String &msg);

            explicit UnsupportedOperationException(String &&msg);

            UnsupportedOperationException();
        }; //class UnsupportedOperationException

    } //namespace java
} //namespace lang
