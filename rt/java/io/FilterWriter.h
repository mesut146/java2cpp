#pragma once

#include "java/io/Writer.h"

namespace java {
    namespace io {

        class FilterWriter : public Writer {
            //fields
        public:
            Writer *out;

            void write(int) override;

            void write(std::vector<wchar_t> *, int, int) override;

            void write(std::string *, int, int) override;

            void write(std::string *s) override;

            explicit FilterWriter(Writer *);

            void close() override;

            void flush() override;

        }; //class FilterWriter

    } //namespace java
} //namespace io
