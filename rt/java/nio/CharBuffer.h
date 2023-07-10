#pragma once

#include <vector>
#include "Buffer.h"

namespace java {
    namespace nio {
        class CharBuffer : public Buffer {
            int offset;
            std::vector<wchar_t> *hb;
            bool isReadOnly;
        public:
            CharBuffer *put(std::vector<wchar_t> *buf, int off, int len);

            virtual CharBuffer *put(wchar_t ch) = 0;
        };
    }
}