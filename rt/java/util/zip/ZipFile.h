#pragma once

#include <string>
#include <vector>
#include "java/io/File.h"
#include "java/io/InputStream.h"
#include "ZipEntry.h"


namespace java {
    namespace util {
        namespace zip {

            class ZipFile {
//fields
            public:
                static int DEFLATED;
                static int JZENTRY_COMMENT;
                static int JZENTRY_EXTRA;
                static int JZENTRY_NAME;
                static int OPEN_DELETE;
                static int OPEN_READ;
                static int STORED;
                bool closeRequested;
                static bool ensuretrailingslash;
                long jzfile;
                bool locsig;
                std::string *name;
                int total;

//methods
            public:
                ZipFile(std::string *);

                ZipFile(java::io::File *);

                ZipFile(java::io::File *, int);


                void close();

                static void close(long);

                void ensureOpen();

                void ensureOpenOrZipException();

                std::vector<ZipEntry *> *entries();

                void finalize();

                static void freeEntry(long, long);

                std::string *getComment();

                static std::vector<char> *getCommentBytes(long);

                ZipEntry *getEntry(std::string *);

                static long getEntry(long, std::vector<char> *, bool);

                static std::vector<char> *getEntryBytes(long, int);

                static long getEntryCSize(long);

                static long getEntryCrc(long);

                static int getEntryFlag(long);

                static int getEntryMethod(long);

                static long getEntrySize(long);

                static long getEntryTime(long);


                java::io::InputStream *getInputStream(ZipEntry *);

                std::string *getName();

                static long getNextEntry(long, int);

                static int getTotal(long);

                ZipEntry *getZipEntry(std::string *, long);

                static std::string *getZipMessage(long);

                static void initIDs();

                static long open(std::string *, int, long, bool);

                static int read(long, long, long, std::vector<char> *, int, int);

                int size();

                static bool startsWithLOC(long);

                bool startsWithLocHeader();


            };//class ZipFile

        }//namespace java
    }//namespace util
}//namespace zip
