#pragma once

#include "java/io/FilterInputStream.h"
#include "java/io/DataInput.h"

namespace java
{
    namespace io
    {

        class DataInputStream : public FilterInputStream, public DataInput
        {
            //fields
        public:
            std::vector<char> *bytearr;
            std::vector<wchar_t> *chararr;
            std::vector<wchar_t> *lineBuffer;
            std::vector<char> *readBuffer;

            //methods
        public:
            explicit DataInputStream(InputStream * in);

            int read(std::vector<char> * arr) override;

            int read(std::vector<char> * arr, int off, int len) override;

            bool readBoolean();

            char readByte();

            wchar_t readChar();

            double readDouble();

            float readFloat();

            void readFully(std::vector<char> *);

            void readFully(std::vector<char> *, int, int) override;

            int readInt();

            std::string *readLine();

            long readLong();

            char16_t readShort();

            std::string *readUTF();

            static std::string *readUTF(DataInput *);

            int readUnsignedByte();

            int readUnsignedShort() override;

            int skipBytes(int);

        }; //class DataInputStream

    } //namespace java
} //namespace io
