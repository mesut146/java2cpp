#pragma once

#include "java/lang/Exception.h"

namespace java {
    namespace nio {
        class BufferOverflowException : public java::lang::Exception {
        public:
            BufferOverflowException();
        };
    }
}

