#include "java/lang/NullPointerException.h"
#include "FileOutputStream.h"

using namespace java::io;
using namespace java::lang;

FileOutputStream::FileOutputStream(File *file) : FileOutputStream(file, false) {

}

FileOutputStream::FileOutputStream(std::string *name) : FileOutputStream(name != nullptr ? new File(name) : nullptr,
                                                                         false) {

}

FileOutputStream::FileOutputStream(std::string *name, bool append) : FileOutputStream(
        name != nullptr ? new File(name) : nullptr, append) {
}

FileOutputStream::FileOutputStream(File *file, bool append) {
    auto name = file != nullptr ? file->getPath() : nullptr;
    if (name == nullptr) {
        throw new NullPointerException();
    }
//    if (file.isInvalid()) {
//        throw new FileNotFoundException("Invalid file path");
//    }
    this->append = append;
    //open
    stream.open(*name);
    if (!stream.is_open()) {
        throw new java::lang::Exception("file can't opened: " + *name);
    }
}

void FileOutputStream::write(int b) {
    int bytesWritten = 0;
    try {
        write(b, append);
        bytesWritten = 1;
    }
    catch (...) {
        //IoTrace.fileWriteEnd(traceContext, bytesWritten);
    }
}

void FileOutputStream::write(int i, bool b) {
    if (b) {
        stream.seekp(std::ofstream::end);
        stream.operator<<(i);
    } else {
        stream.operator<<(i);
    }
}

void FileOutputStream::writeBytes(std::vector<char> *b, int off, int len, bool append) {
    if (append) {
        stream.seekp(std::ofstream::end);
    }
    stream.write(b->data() + off, len);
}

void FileOutputStream::write(std::vector<char> *b) {
    int bytesWritten = 0;
    try {
        writeBytes(b, 0, b->size(), append);
        bytesWritten = b->size();
    }
    catch (...) {
        //IoTrace.fileWriteEnd(traceContext, bytesWritten);
    }
}

void FileOutputStream::writeBytes(std::vector<char> *b, int off, int len) {
    int bytesWritten = 0;
    try {
        writeBytes(b, off, len, append);
        bytesWritten = len;
    } catch (...) {
        //IoTrace.fileWriteEnd(traceContext, bytesWritten);
    }
}

void FileOutputStream::close() {
    if (closed) {
        return;
    }
    closed = true;
    stream.close();
}
