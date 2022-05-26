#pragma once

#include "java/io/OutputStream.h"

namespace java {
    namespace io {

        class ByteArrayOutputStream : public OutputStream {
//fields
        public:
            static const int MAX_ARRAY_SIZE = INT32_MAX - 8;
            std::vector<char> *buf;
            int count;

//methods
        public:
            ByteArrayOutputStream();

            explicit ByteArrayOutputStream(int);

            void close() override;

            void ensureCapacity(int);

            void grow(int);

            static int hugeCapacity(int);

            void reset();

            int size();

            std::vector<char> *toByteArray();

            std::string *toString();

            std::string *toString(std::string *);

            std::string *toString(int);

            void write(int) override;

            void write(std::vector<char> *, int, int) override;

            void writeTo(OutputStream *);


        };//class ByteArrayOutputStream

    }//namespace java
}//namespace io
