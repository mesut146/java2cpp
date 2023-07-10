#pragma once

#include "java/io/OutputStream.h"

namespace java {
    namespace io {

        class FilterOutputStream : public OutputStream {
//fields
        public:
            OutputStream *out;

//methods
        public:
            explicit FilterOutputStream(OutputStream *);

            void close() override;

            void flush() override;

            void write(int) override;

            void write(std::vector<char> *, int, int) override;

            void write(std::vector<char> *) override;


        };//class FilterOutputStream

    }//namespace java
}//namespace io
