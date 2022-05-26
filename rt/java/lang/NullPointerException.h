#pragma once

#include "java/lang/RuntimeException.h"

namespace java {
    namespace lang {

        class NullPointerException : public RuntimeException {
        public:
            explicit NullPointerException(std::string *msg);
            explicit NullPointerException(std::string &&msg);

            NullPointerException();
        }; //class NullPointerException

    } //namespace java
} //namespace lang
