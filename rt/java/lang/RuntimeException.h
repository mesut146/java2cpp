#pragma once

#include "java/lang/Exception.h"

namespace java {
    namespace lang {

        class RuntimeException : public Exception {
            //methods
        public:
            explicit RuntimeException(std::string *);

            explicit RuntimeException(std::string &&s);

            explicit RuntimeException(const String &s);

            explicit RuntimeException(Throwable *);

            RuntimeException(std::string *, Throwable *);

            RuntimeException();

        }; //class RuntimeException

    } //namespace java
} //namespace lang
