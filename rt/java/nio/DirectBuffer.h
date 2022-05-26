#pragma once

#include "Buffer.h"

namespace java {
    namespace nio {
        //sun nio ch
        class DirectBuffer {
        public:
            virtual long address() = 0;
        };
    }
}