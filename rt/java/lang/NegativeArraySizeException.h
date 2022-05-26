#pragma once

#include "RuntimeException.h"

namespace java {
    namespace lang {
        class NegativeArraySizeException : public RuntimeException {
        public:
            explicit NegativeArraySizeException(std::string *msg) : RuntimeException(msg) {}
        };
    }

} // namespace lang
