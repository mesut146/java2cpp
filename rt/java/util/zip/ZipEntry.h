#pragma once

#include <string>
#include <vector>

namespace java {
    namespace util {
        namespace zip {

            class ZipEntry {
//fields
            public:
                static int DEFLATED;
                static long DOSTIME_BEFORE_1980;
                static int STORED;
                static long UPPER_DOSTIME_BOUND;
                std::string *comment;
                long crc;
                long csize;
                std::vector<char> *extra;
                int flag;
                int method;
                std::string *name;
                long size;
                long xdostime;

//methods
            public:
                ZipEntry();

                ZipEntry(std::string *);

                ZipEntry(ZipEntry *);

                //java::lang::Object *clone();

                std::string *getComment();

                long getCompressedSize();

                long getCrc();

                std::vector<char> *getExtra();


                int getMethod();

                std::string *getName();

                long getSize();

                long getTime();

                int hashCode();

                bool isDirectory();

                void setComment(std::string *);

                void setCompressedSize(long);

                void setCrc(long);

                void setExtra(std::vector<char> *);

                void setExtra0(std::vector<char> *, bool);

                void setMethod(int);

                void setSize(long);

                void setTime(long);

                std::string *toString();


            };//class ZipEntry

        }//namespace java
    }//namespace util
}//namespace zip
