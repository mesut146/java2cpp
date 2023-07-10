#pragma once

#include "java/lang/RuntimeException.h"

namespace java {
    namespace lang {

        class IndexOutOfBoundsException : public RuntimeException {
        public:
            IndexOutOfBoundsException();

            explicit IndexOutOfBoundsException(std::string *msg);
        }; //class IndexOutOfBoundsException

    } //namespace java
} //namespace lang
