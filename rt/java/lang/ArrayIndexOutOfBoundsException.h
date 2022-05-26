#pragma once

#include "RuntimeException.h"

namespace java {
    namespace lang {
        class ArrayIndexOutOfBoundsException : RuntimeException {
        public:
            explicit ArrayIndexOutOfBoundsException(std::string *msg);

            ArrayIndexOutOfBoundsException();

            ArrayIndexOutOfBoundsException(int index);
        };

    } // namespace lang

} // namespace java
