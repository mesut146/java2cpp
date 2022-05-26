#pragma once

#include "java/io/Writer.h"
#include "java/lang/StringBuffer.h"

namespace java
{
    namespace io
    {

        class StringWriter : public Writer
        {
            //fields
        public:
            java::lang::StringBuffer *buf;

            //methods
        public:
            StringWriter();

            explicit StringWriter(int);

            //StringWriter *append(java::lang::CharSequence *);

            StringWriter *append(wchar_t) override;

            //StringWriter *append(java::lang::CharSequence *, int, int);

            void close() override;

            void flush() override;

            java::lang::StringBuffer *getBuffer() const;

            std::string *toString() const;

            void write(int) override;

            void write(std::string *) override;

            void write(std::vector<wchar_t> *, int, int) override;

            void write(std::string *, int, int) override;

        }; //class StringWriter

    } //namespace java
} //namespace io
