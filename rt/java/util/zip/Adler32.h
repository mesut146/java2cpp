#pragma once

#include "java/util/zip/Checksum.h"
#include "java/nio/ByteBuffer.h"
#include <vector>

namespace java {
    namespace util {
        namespace zip {

            class Adler32 : public Checksum {
            public:
                int adler = 1;

                Adler32();

                long getValue() const;

                void reset();

                void update(int);

                void update(std::vector<char> *);

                void update(java::nio::ByteBuffer *);

                static int update(int adler, int b);

                void update(std::vector<char> *, int, int);

                static int updateByteBuffer(int adler, long addr, int off, int len);

                static int updateBytes(int adler, std::vector<char> *b, int off, int len);


            };//class Adler32

        }//namespace java
    }//namespace util
}//namespace zip
