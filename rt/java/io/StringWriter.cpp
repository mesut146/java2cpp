#include "java/lang/IllegalArgumentException.h"
#include "java/lang/IndexOutOfBoundsException.h"
#include "StringWriter.h"

using namespace java::io;
using namespace java::lang;

StringWriter::StringWriter() {
    buf = new StringBuffer();
}

StringWriter::StringWriter(int initialSize) {
    if (initialSize < 0) {
        throw IllegalArgumentException(new std::string("Negative buffer size"));
    }
    buf = new StringBuffer(initialSize);
}

void StringWriter::flush() {

}

void StringWriter::close() {

}

StringWriter *StringWriter::append(wchar_t c) {
    write(c);
    return this;
}

std::string *StringWriter::toString() const {
    return buf->toString();
}

StringBuffer *StringWriter::getBuffer() const {
    return buf;
}

void StringWriter::write(int i) {
    buf->append((wchar_t) i);
}

void StringWriter::write(std::vector<wchar_t> *cbuf, int off, int len) {
    if ((off < 0) || (off > cbuf->size()) || (len < 0) ||
        ((off + len) > cbuf->size()) || ((off + len) < 0)) {
        throw IndexOutOfBoundsException();
    } else if (len == 0) {
        return;
    }
    buf->append(cbuf, off, len);
}

void StringWriter::write(std::string *s) {
    buf->append(s);
}

void StringWriter::write(std::string *s, int off, int len) {
    auto a = s->substr(off, off + len);
    buf->append(&a);
}


