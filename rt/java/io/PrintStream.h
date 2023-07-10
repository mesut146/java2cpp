#pragma once

#include <string>
#include <vector>
#include "java/io/FilterOutputStream.h"
#include "java/lang/CharSequence.h"
#include "OutputStreamWriter.h"
#include "BufferedWriter.h"

namespace java {
    namespace io {

        class PrintStream : public FilterOutputStream {
        public:

            BufferedWriter *textOut;
            OutputStreamWriter *charOut;
            bool closing = false;
            bool trouble = false;
            bool autoFlush = false;

            virtual ~PrintStream();

            explicit PrintStream(OutputStream *);

            //PrintStream(std::string *);

            //PrintStream(File *);

            PrintStream(bool, OutputStream *);

            //PrintStream(OutputStream *, bool);

            //PrintStream(std::string *, std::string *);

            // PrintStream(File *, std::string *);

            //PrintStream(OutputStream *, bool, std::string *);

            //PrintStream *append(java::lang::CharSequence *);

            PrintStream *append(wchar_t ch);

            //PrintStream *append(java::lang::CharSequence *, int, int);


            void close() override;


            void flush() override;

            //PrintStream *format(std::string *, std::vector<java::lang::Object *> *);

            //PrintStream *format(java::util::Locale *, std::string *, std::vector<java::lang::Object *> *);

            void print(bool);

            //void print(wchar_t ch);

            // void print(int);

            //void print(long);

            //void print(float);

            //void print(double);

            // void print(std::vector<wchar_t> *);

            void print(std::string * s);

            //void print(java::lang::Object *);

            //PrintStream *printf(std::string *, std::vector<java::lang::Object *> *);

            //PrintStream* printf(java::util::Locale* , std::string* , std::vector<java::lang::Object*>* );

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

            void write(int i) override;

            void write(std::vector<wchar_t> *arr);

            void write(std::string *);

            void write(std::vector<char> *arr, int off, int len) override;

            void newLine();


            void ensureOpen();
        };//class PrintStream

    }//namespace java
}//namespace io
