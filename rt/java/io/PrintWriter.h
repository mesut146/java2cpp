#pragma once

#include "java/io/Writer.h"

namespace java {
    namespace io {

        class PrintWriter : public Writer {
        public:
            bool autoFlush;
            std::string *lineSeparator = new std::string("\n");
            Writer *out;

            //methods
            explicit PrintWriter(Writer *);

            PrintWriter(Writer *, bool);

            PrintWriter(OutputStream *os, bool fl);

            explicit PrintWriter(OutputStream *os);

            //PrintWriter(std::string *);


            //PrintWriter(java::nio::charset::Charset* , File* );

            //PrintWriter(std::string *, std::string *);

            //PrintWriter(File *, std::string *);

            //PrintWriter *append(java::lang::CharSequence *);

            PrintWriter *append(wchar_t) override;

            //PrintWriter *append(java::lang::CharSequence *, int, int);

            void close() override;

            void flush() override;

            //PrintWriter *format(std::string *, std::vector<java::lang::Object *> *);

            //PrintWriter *format(java::util::Locale *, std::string *, std::vector<java::lang::Object *> *);


            void print(bool);

            void print(wchar_t);

            void print(int);

            void print(long);

            void print(float);

            void print(double);

            void print(std::vector<wchar_t> *);

            void print(std::string *);

            //void print(java::lang::Object *);

            //PrintWriter *printf(std::string *, std::vector<java::lang::Object *> *);

            //PrintWriter *printf(java::util::Locale *, std::string *, std::vector<java::lang::Object *> *);

            void println();

            void println(bool);

            void println(wchar_t);

            void println(int);

            void println(long);

            void println(float);

            void println(double);

            void println(std::vector<wchar_t> *);

            void println(std::string *);

            //void println(java::lang::Object *);

            //void setError();

            //static java::nio::charset::Charset* toCharset(std::string* );

            void write(int i) override;

            void write(std::vector<wchar_t> *buf) override;

            void write(std::string *s) override;

            void write(std::vector<wchar_t> *arr, int off, int len) override;

            void write(std::string *s, int off, int len) override;

            void newLine();

        }; //class PrintWriter

    } //namespace java
} //namespace io
