#pragma once

#include <vector>
#include <string>

namespace java {
    namespace io {

        class Writer {
            //fields
        public:
            const int WRITE_BUFFER_SIZE = 1024;
            std::vector<wchar_t> *writeBuffer;

            //methods
        public:
            Writer();

            virtual void write(int);

            virtual void write(std::vector<wchar_t> *, int, int) = 0;

            virtual void write(std::string *, int, int);

            virtual Writer *append(std::string *);

            virtual Writer *append(wchar_t);

            virtual Writer *append(std::string *, int, int);

            virtual void close() = 0;

            virtual void flush() = 0;

            virtual void write(std::vector<wchar_t> *);

            virtual void write(std::string *);

        }; //class Writer

    } //namespace java
} //namespace io
