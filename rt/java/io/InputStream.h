#pragma once

#include <vector>

namespace java {
    namespace io {

        class InputStream {
            //fields
        public:
            //static constexpr int MAX_SKIP_BUFFER_SIZE = 2048;

            //methods
        public:
            InputStream();

            //virtual int available();

            //virtual void mark(int);

            //virtual bool markSupported();

            virtual int read() = 0;

            virtual int read(std::vector<char> *, int, int);

            virtual void reset();

            virtual void close();

            virtual long skip(long);

            virtual int read(std::vector<char> *);

            virtual int available();

        }; //class InputStream

    } //namespace java
} //namespace io
