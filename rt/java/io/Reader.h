#pragma once

#include <vector>
#include "java/nio/CharBuffer.h"

namespace java {
    namespace io {

        class Reader {
            static constexpr int maxSkipBufferSize = 8192;
            std::vector<wchar_t> *skipBuffer = nullptr;
        public:
            virtual ~Reader();

            virtual void close() = 0;

            virtual void mark(int);

            virtual bool markSupported();

            virtual int read();

            virtual int read(std::vector<wchar_t> *, int, int) = 0;

            virtual int read(std::vector<wchar_t> *);

            virtual int read(java::nio::CharBuffer *target);

            virtual bool ready();

            virtual void reset();

            virtual long skip(long);


        };//class Reader

    }//namespace java
}//namespace io
