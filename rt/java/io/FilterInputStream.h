#pragma once

#include <vector>
#include "java/io/InputStream.h"

namespace java {
    namespace io {

        class FilterInputStream : public InputStream {
//fields
        public:
            InputStream *in;

//methods
        public:
            int available() override;

            void close() override;

            explicit FilterInputStream(InputStream *);

            void mark(int);

            bool markSupported();

            int read() override;

            int read(std::vector<char> *) override;

            int read(std::vector<char> *, int, int) override;

            void reset() override;

            long skip(long l) override;


        };//class FilterInputStream

    }//namespace java
}//namespace io
