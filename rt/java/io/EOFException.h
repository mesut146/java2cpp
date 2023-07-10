#pragma once

#include "java/io/IOException.h"

namespace java {
    namespace io {

        class EOFException : public IOException {
        public:
            EOFException();
        };//class EOFException

    }//namespace java
}//namespace io
