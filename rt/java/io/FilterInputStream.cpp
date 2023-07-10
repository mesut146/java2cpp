#include "FilterInputStream.h"

using namespace java::io;
//using namespace java::lang;

FilterInputStream::FilterInputStream(InputStream *in) {
    this->in = in;
}

int FilterInputStream::read() {
    return in->read();
}

void FilterInputStream::reset() {
    in->reset();
}

void FilterInputStream::close() {
    in->close();
}

int FilterInputStream::available() {
    return in->available();
}

long FilterInputStream::skip(long l) {
    return in->skip(l);
}

int FilterInputStream::read(std::vector<char> *arr) {
    return in->read(arr);
}

int FilterInputStream::read(std::vector<char> *arr, int off, int len) {
    return in->read(arr, off, len);
}
