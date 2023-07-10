#pragma once

#include "java/io/Writer.h"

namespace java {
    namespace io {
        class BufferedWriter : public Writer {
        public:
            Writer *out;
            std::vector<wchar_t> *cb;
            std::string lineSeparator = "\n";
            int nChars, nextChar;
            static constexpr int defaultCharBufferSize = 8192;

            explicit BufferedWriter(Writer *w);

            BufferedWriter(Writer *w, int sz);

            void close() override;

            void flush() override;

            void flushBuffer();

            void newLine();

            void ensureOpen();

            void write(int c) override;

            void write(std::string *s, int off, int len) override;

            void write(std::vector<wchar_t> *cbuf, int off, int len) override;
        };
    }
}


