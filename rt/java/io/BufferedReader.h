#pragma once

#include <vector>
#include <string>
#include "java/io/Reader.h"

namespace java {
    namespace io {

        class BufferedReader : public Reader {
//fields
        public:


            static constexpr int INVALIDATED = -2;
            static constexpr int UNMARKED = -1;
            std::vector<wchar_t> *cb;
            static constexpr int defaultCharBufferSize = 8192;
            static constexpr int defaultExpectedLineLength = 80;
            Reader *in;
            int markedChar = UNMARKED;
            bool markedSkipLF = false;
            int nChars;
            int nextChar;
            int readAheadLimit = 0;
            bool skipLF = false;

//methods
        public:
            BufferedReader(Reader *in);

            BufferedReader(Reader *in, int sz);

            virtual ~BufferedReader();

            void close();

            void ensureOpen();

            void fill();

            void mark(int);

            bool markSupported();

            int read();

            int read(std::vector<wchar_t> *, int, int);

            int read1(std::vector<wchar_t> *, int, int);

            std::string *readLine();

            std::string *readLine(bool ignoreLF);

            bool ready();

            void reset();

            long skip(long);


        };//class BufferedReader

    }//namespace java
}//namespace io
