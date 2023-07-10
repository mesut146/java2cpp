#pragma once

#include "java/lang/Object.h"

namespace java {
    namespace io {

        class File {
            //fields
        public:
            std::string *path;
            int prefixLength = 0;
            static long PATH_OFFSET;
            static long PREFIX_LENGTH_OFFSET;
            static std::string *pathSeparator();
            static const wchar_t pathSeparatorChar = ':';
            static std::string *separator();
            static const wchar_t separatorChar = '/';
            //PathStatus *status;

            //methods
        public:
            explicit File(std::string *);

            //File(std::string *path, int flags);

            //File(std::string *parent, File * file);

            //File(std::string *parent, std::string *name);

            File(File *, std::string *);

            //bool canExecute();

            bool canRead();

            //bool canWrite();

            int compareTo(File *) const;

            //bool createNewFile();

            //static File *createTempFile(std::string *, std::string *);

            //static File *createTempFile(std::string *, std::string *, File *);

            //bool delete_renamed();

            //void deleteOnExit();

            bool equals(java::lang::Object *) const;

            bool exists();

            //File *getAbsoluteFile();

            //std::string *getAbsolutePath();

            //File *getCanonicalFile();

            // std::string *getCanonicalPath();

            //long getFreeSpace();

            std::string *getName() const;

            std::string *getParent() const;

            // File *getParentFile();

            std::string *getPath() const;

            //int getPrefixLength();

            //long getTotalSpace();

            //long getUsableSpace();

            int hashCode() const;

            //bool isAbsolute();

            bool isDirectory();

            bool isFile();

            //bool isHidden();

            //bool isInvalid();

            long lastModified();

            long length();

            std::vector<std::string *> *list();

            //std::vector<std::string *> *list(FilenameFilter *);

            std::vector<File *> *listFiles();

            //std::vector<File *> *listFiles(FilenameFilter *);

            //std::vector<File *> *listFiles(FileFilter *);

            //static std::vector<File *> *listRoots();

            //bool mkdir();

            //bool mkdirs();

            //void readObject(ObjectInputStream *);

            //bool renameTo(File *);

            //bool setExecutable(bool);

            //bool setExecutable(bool, bool);

            //bool setLastModified(long);

            //bool setReadOnly();

            //bool setReadable(bool);

            //bool setReadable(bool, bool);

            //bool setWritable(bool);

            //bool setWritable(bool, bool);

            // static std::string *slashify(std::string *, bool);

            //java::nio::file::Path *toPath();

            std::string *toString() const;

            //java::net::URI *toURI();

            // java::net::URL *toURL();

            //void writeObject(ObjectOutputStream *);

            File *getParentFile() const;

            void mkdirs();

            void createNewFile();
        }; //class File

    } //namespace java
} //namespace io
