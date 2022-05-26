#include "java/lang/IllegalArgumentException.h"
#include "Reader.h"
#include "java/nio/CharBuffer.h"
#include "IOException.h"

using namespace java::io;

Reader::~Reader() = default;


int Reader::read(java::nio::CharBuffer *target) {
    int len = target->remaining();
    auto *cbuf = new std::vector<wchar_t>(len);
    int n = read(cbuf, 0, len);
    if (n > 0)
        target->put(cbuf, 0, n);

    return n;
}

int Reader::read() {
    auto cb = new std::vector<wchar_t>(1);
    if (read(cb, 0, 1) == -1)
        return -1;
    else return cb->at(0);

}

int Reader::read(std::vector<wchar_t> *cbuf) {
    return read(cbuf, 0, cbuf->size());
}

long Reader::skip(long n) {
    if (n < 0L)
        throw new java::lang::IllegalArgumentException(new std::string("skip value is negative"));

    int nn = (int) std::min(n, (long) Reader::maxSkipBufferSize);
    {
        //synchronized
        if ((skipBuffer == nullptr) || (skipBuffer->size() < nn))
            skipBuffer = new std::vector<wchar_t>(nn);

        long r = n;
        while (r > 0) {
            int nc = read(skipBuffer, 0, (int) std::min(r, (long) nn));
            if (nc == -1)
                break;

            r -= nc;
        }

        return n - r;
    }
}

bool Reader::ready() {
    return false;
}

bool Reader::markSupported() {
    return false;
}

void Reader::mark(int readAheadLimit) {
    throw new IOException(new std::string("mark() not supported"));
}

void Reader::reset() {
    throw new IOException(new std::string("reset() not supported"));
}