#pragma once

#include "OutputStream.h"
#include "File.h"
#include <fstream>

namespace java {
    namespace io {

        class FileOutputStream : public OutputStream {
            File *file;
            std::string *path;
            bool append;
            bool closed = false;
            std::ofstream stream;
        public:
            explicit FileOutputStream(File *file);

            FileOutputStream(File *file, bool append);

            FileOutputStream(std::string *name, bool append);

            explicit FileOutputStream(std::string *name);

            void write(int b) override;

            void write(int i, bool b);

            void write(std::vector<char> *b) override;

            void writeBytes(std::vector<char> *b, int off, int len, bool append);

            void writeBytes(std::vector<char> *b, int off, int len);

            void close() override;
        }; //class FileOutputStream

    } //namespace java
} //namespace io
