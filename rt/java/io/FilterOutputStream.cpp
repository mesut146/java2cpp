#include "java/lang/IndexOutOfBoundsException.h"
#include "FilterOutputStream.h"
#include "IOException.h"

using namespace java::io;
using namespace java::lang;

FilterOutputStream::FilterOutputStream(OutputStream *os) {
    this->out = os;
}

void FilterOutputStream::flush() {
    out->flush();
}

void FilterOutputStream::close() {
    try {
        flush();
    } catch (IOException ignored) {
    }
    out->close();
}

void FilterOutputStream::write(int i) {
    out->write(i);
}

void FilterOutputStream::write(std::vector<char> *b, int off, int len) {
    if ((off | len | (b->size() - (len + off)) | (off + len)) < 0)
        throw new IndexOutOfBoundsException();

    for (int i = 0; i < len; i++) {
        write((*b)[off + i]);
    }
}

void FilterOutputStream::write(std::vector<char> *b) {
    write(b, 0, b->size());
}
