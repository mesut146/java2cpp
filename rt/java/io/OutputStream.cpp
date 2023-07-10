#include "java/lang/NullPointerException.h"
#include "java/lang/IndexOutOfBoundsException.h"
#include "OutputStream.h"

using namespace java::io;

void OutputStream::close() {}

void OutputStream::flush() {}

void OutputStream::write(std::vector<char> *arr) {
    write(arr, 0, arr->size());
}

void OutputStream::write(std::vector<char> *b, int off, int len) {
    if (b == nullptr) {
        throw new lang::NullPointerException();
    } else if ((off < 0) || (off > b->size()) || (len < 0) ||
               ((off + len) > b->size()) || ((off + len) < 0)) {
        throw new lang::IndexOutOfBoundsException();
    } else if (len == 0) {
        return;
    }
    for (int i = 0; i < len; i++) {
        write((*b)[off + i]);
    }
}