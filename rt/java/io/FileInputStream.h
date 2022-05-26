#pragma once

#include <string>
#include "java/io/InputStream.h"
#include "java/io/File.h"
#include <fstream>

namespace java {
    namespace io {

        class FileInputStream : public InputStream {
            //fields
        public:
            bool closed;
            std::string *path;
            File *file;
            std::ifstream stream;

            //methods
        public:
            explicit FileInputStream(std::string *path);

            explicit FileInputStream(File *file);

            //FileInputStream(FileDescriptor *);

            virtual ~FileInputStream();

            int available() override;

            int available0();

            void close() override;

            void close0();

            void finalize();

            //java::nio::channels::FileChannel *getChannel();

            //FileDescriptor *getFD();

            //static void initIDs();

            //void open(std::string *);

            //void open0(std::string *);

            int read() override;

            int read(std::vector<char> *) override;

            int read(std::vector<char> *, int, int) override;

            int read0();

            int readBytes(std::vector<char> *, int, int);

            long skip(long) override;

            long skip0(long);

        }; //class FileInputStream

    } //namespace java
} //namespace io
