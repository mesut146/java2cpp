#include <string>
#include <vector>
#include "java/lang/Exception.h"
#include "File.h"
#include "FileInputStream.h"

using namespace java::io;

FileInputStream::FileInputStream(File *f) {
    this->file = f;
    this->path = f->getPath();
    stream.open(*path, std::ios::binary);
    if (!stream.is_open()) {
        throw new java::lang::Exception("file cant be opened: " + *path);
    }
}

FileInputStream::FileInputStream(std::string *s) : FileInputStream(new File(s)) {
}

FileInputStream::~FileInputStream() = default;

int FileInputStream::read() {
    return stream.get();
}

int FileInputStream::read(std::vector<char> *arr, int off, int len) {
    /*char *buf = new char[len - off];
    stream.get(buf, len);
    arr->insert(off + arr->begin(), buf, buf + len - off);
    return len - off;*/
    return InputStream::read(arr, off, len);
}

int FileInputStream::read(std::vector<char> *arr) {
    return read(arr, 0, arr->size());
}

void FileInputStream::close() {
    stream.close();
}

long FileInputStream::skip(long l) {
    stream.ignore(l);
    return l;
}

int FileInputStream::available() {
    throw new java::lang::Exception("FileInputStream::available()");
}