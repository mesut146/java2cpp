#pragma once

#include "java/io/Writer.h"
#include "java/io/OutputStream.h"

namespace java {
    namespace io {

        class OutputStreamWriter : public Writer {

            OutputStream *os;
        public:
            explicit OutputStreamWriter(OutputStream *);

            //OutputStreamWriter(OutputStream *, std::string *);

            //OutputStreamWriter(OutputStream* , java::nio::charset::Charset* );

            //OutputStreamWriter(OutputStream* , java::nio::charset::CharsetEncoder* );

            void close() override;

            void flush() override;

            //void flushBuffer();

            //std::string *getEncoding();

            void write(int) override;

            void write(std::vector<wchar_t> *, int, int) override;

            void write(std::string *, int, int) override;


        };//class OutputStreamWriter

    }//namespace java
}//namespace io
