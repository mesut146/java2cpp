#include <cstdint>
#include "java/lang/IndexOutOfBoundsException.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/io/ByteArrayOutputStream.h"
#include "CppHelper.h"

using namespace java::io;

//methods
ByteArrayOutputStream::ByteArrayOutputStream() : ByteArrayOutputStream(32) {
}

ByteArrayOutputStream::ByteArrayOutputStream(int size) {
    if (size < 0) {
        throw new java::lang::IllegalArgumentException(new std::string("Negative initial size: ") + size);
    }

    buf = new std::vector<char>(size);
}

void ByteArrayOutputStream::ensureCapacity(int minCapacity) {
    if (minCapacity - buf->size() > 0)
        grow(minCapacity);

}

void ByteArrayOutputStream::grow(int minCapacity) {
    int oldCapacity = buf->size();
    int newCapacity = oldCapacity << 1;
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;

    if (newCapacity - ByteArrayOutputStream::MAX_ARRAY_SIZE > 0)
        newCapacity = ByteArrayOutputStream::hugeCapacity(minCapacity);

    buf = copyOfRange(buf, 0, newCapacity);
}

int ByteArrayOutputStream::hugeCapacity(int minCapacity) {
    if (minCapacity < 0)
        throw new java::lang::Exception(new std::string("out of mem"));

    return (minCapacity > ByteArrayOutputStream::MAX_ARRAY_SIZE) ? INT32_MAX
                                                                 : ByteArrayOutputStream::MAX_ARRAY_SIZE;
}

void ByteArrayOutputStream::write(int b) {
    ensureCapacity(count + 1);
    buf->at(count) = (char) b;
    count += 1;
}

void ByteArrayOutputStream::write(std::vector<char> *b, int off, int len) {
    if ((off < 0) || (off > b->size()) || (len < 0) || ((off + len) - b->size() > 0)) {
        throw new java::lang::IndexOutOfBoundsException();
    }

    ensureCapacity(count + len);
    arraycopy(b, off, buf, count, len);
    count += len;
}

void ByteArrayOutputStream::writeTo(OutputStream *out) {
    out->write(buf, 0, count);
}

void ByteArrayOutputStream::reset() {
    count = 0;
}

std::vector<char> *ByteArrayOutputStream::toByteArray() {
    return copyOfRange(buf, 0, count);
}

int ByteArrayOutputStream::size() {
    return count;
}

std::string *ByteArrayOutputStream::toString() {
    return makeString(buf, 0, count);
}

std::string *ByteArrayOutputStream::toString(std::string *charsetName) {
    throw java::lang::Exception(new std::string("ByteArrayOutputStream::toString charset"));
    //return new java::lang::String(buf, 0, count, charsetName);
}

std::string *ByteArrayOutputStream::toString(int hibyte) {
    throw java::lang::Exception(new std::string("ByteArrayOutputStream::toString hibyte"));
    //return makeString(buf,hibyte,0);
    //return new java::lang::String(buf, hibyte, 0, count);
}

void ByteArrayOutputStream::close() {
}

