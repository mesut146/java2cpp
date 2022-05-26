#include "java/lang/IndexOutOfBoundsException.h"
#include "java/lang/IllegalArgumentException.h"
#include "IOException.h"
#include "CppHelper.h"
#include "BufferedWriter.h"

using namespace java::io;
using namespace java::lang;

int min(int a, int b) {
    if (a < b) return a;
    return b;
}

BufferedWriter::BufferedWriter(Writer *w) : BufferedWriter(w, defaultCharBufferSize) {
}

void BufferedWriter::close() {
    if (out == nullptr) return;
    flushBuffer();
    out->close();
    out = nullptr;
}

void BufferedWriter::flush() {
    out->flush();
}

BufferedWriter::BufferedWriter(Writer *out, int sz) {
    if (sz <= 0)
        throw new lang::IllegalArgumentException(new std::string("Buffer size <= 0"));
    this->out = out;
    cb = new std::vector<wchar_t>(sz);
    nChars = sz;
    nextChar = 0;
    lineSeparator = "\n";
}

void BufferedWriter::flushBuffer() {
    ensureOpen();
    if (nextChar == 0)
        return;
    out->write(cb, 0, nextChar);
    nextChar = 0;
}

void BufferedWriter::ensureOpen() {
    if (out == nullptr)
        throw new IOException(new std::string("Stream closed"));
}

void BufferedWriter::write(int c) {
    ensureOpen();
    if (nextChar >= nChars)
        flushBuffer();
    (*cb)[nextChar++] = (char) c;
}

void BufferedWriter::write(std::string *s, int off, int len) {
    ensureOpen();

    int b = off, t = off + len;
    while (b < t) {
        int d = min(nChars - nextChar, t - b);
        CppHelper::getChars(s, b, b + d, cb, nextChar);
        b += d;
        nextChar += d;
        if (nextChar >= nChars)
            flushBuffer();
    }
}

void BufferedWriter::write(std::vector<wchar_t> *cbuf, int off, int len) {
    ensureOpen();
    if ((off < 0) || (off > cbuf->size()) || (len < 0) ||
        ((off + len) > cbuf->size()) || ((off + len) < 0)) {
        throw new IndexOutOfBoundsException();
    } else if (len == 0) {
        return;
    }

    if (len >= nChars) {
        /* If the request length exceeds the size of the output buffer,
           flush the buffer and then write the data directly.  In this
           way buffered streams will cascade harmlessly. */
        flushBuffer();
        out->write(cbuf, off, len);
        return;
    }

    int b = off, t = off + len;
    while (b < t) {
        int d = min(nChars - nextChar, t - b);
        arraycopy(cbuf, b, cb, nextChar, d);
        b += d;
        nextChar += d;
        if (nextChar >= nChars)
            flushBuffer();
    }
}

void BufferedWriter::newLine() {
    Writer::write(&lineSeparator);
}

