#include <vector>
#include <string>
#include "CppHelper.h"
#include "Writer.h"

using namespace java::io;

Writer::Writer() {

}

void Writer::write(int c) {
    if (writeBuffer == nullptr) {
        writeBuffer = new std::vector<wchar_t>(WRITE_BUFFER_SIZE);
    }
    writeBuffer->at(0) = c;
    write(writeBuffer, 0, 1);
}

void Writer::write(std::string *s) {
    write(s, 0, s->length());
}

//template<class E, class T>
//E *cast(const T *arr, int n) {
//    E *res = new E[n];
//    for (int i = 0; i < n; i++) {
//        res[i] = (E) arr[i];
//    }
//    return res;
//}

void Writer::write(std::vector<wchar_t> *arr) {
    write(arr, 0, arr->size());
}

void Writer::write(std::string *s, int off, int len) {
    std::vector<wchar_t> cbuf;
    if (len <= WRITE_BUFFER_SIZE) {
        if (writeBuffer == nullptr) {
            writeBuffer = new std::vector<wchar_t>(WRITE_BUFFER_SIZE);
        }
        cbuf = *writeBuffer;
    } else {    // Don't permanently allocate very large buffers.
        cbuf = std::vector<wchar_t>(len);
    }
    int i = 0;
    while (off < (off + len)) {
        cbuf[i++] = (*s)[off];
        off++;
    }
    write(&cbuf, 0, len);
}

Writer *Writer::append(wchar_t c) {
    write(c);
    return this;
}

Writer *Writer::append(std::string *s) {
    write(s, 0, s->size());
    return this;
}

Writer *Writer::append(std::string *s, int off, int len) {
    write(s, off, len);
    return this;
}
