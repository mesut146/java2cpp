#include "CppHelper.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/IndexOutOfBoundsException.h"
#include "java/lang/UnsupportedOperationException.h"
#include "java/lang/StringBuffer.h"
#include "Bits.h"
#include "HeapByteBuffer.h"
#include "java/nio/ByteBuffer.h"

using namespace java::nio;
using namespace java::lang;

//methods
ByteBuffer::ByteBuffer(int mark, int pos, int lim, int cap, std::vector<char> *hb, int offset) : Buffer(mark, pos, lim,
                                                                                                        cap) {
    this->hb = hb;
    this->offset = offset;
}

ByteBuffer::ByteBuffer(int mark, int pos, int lim, int cap) : ByteBuffer(mark, pos, lim, cap, nullptr, 0) {
}

/*ByteBuffer *ByteBuffer::allocateDirect(int capacity) {
    return new DirectByteBuffer(capacity);
}*/

/*ByteBuffer *ByteBuffer::allocate(int capacity) {
    if (capacity < 0)
        throw new java::lang::IllegalArgumentException();

    return new HeapByteBuffer(capacity, capacity);
}*/

ByteBuffer *ByteBuffer::wrap(std::vector<char> *array, int offset, int length) {
    try {
        return new HeapByteBuffer(array, offset, length);
    }
    catch (java::lang::IllegalArgumentException &x) {
        throw java::lang::IndexOutOfBoundsException();
    }
}

ByteBuffer *ByteBuffer::wrap(std::vector<char> *array) {
    return ByteBuffer::wrap(array, 0, array->size());
}

ByteBuffer *ByteBuffer::get(std::vector<char> *dst, int offset, int length) {
    Buffer::checkBounds(offset, length, dst->size());
    if (length > Buffer::remaining())
        throw Exception(new std::string("BufferUnderflowException"));

    int end = offset + length;
    for (int i = offset; i < end; i++)
        dst->at(i) = get();

    return this;
}

ByteBuffer *ByteBuffer::get(std::vector<char> *dst) {
    return get(dst, 0, dst->size());
}

ByteBuffer *ByteBuffer::put(ByteBuffer *src) {
    if (src == this)
        throw java::lang::IllegalArgumentException();

    int n = src->remaining();
    if (n > Buffer::remaining())
        throw Exception(new std::string("BufferOverflowException"));

    for (int i = 0; i < n; i++)
        put(src->get());

    return this;
}

ByteBuffer *ByteBuffer::put(std::vector<char> *src, int offset, int length) {
    Buffer::checkBounds(offset, length, src->size());
    if (length > Buffer::remaining())
        throw Exception(new std::string("BufferOverflowException"));

    int end = offset + length;
    for (int i = offset; i < end; i++)
        this->put(src->at(i));

    return this;
}

ByteBuffer *ByteBuffer::put(std::vector<char> *src) {
    return put(src, 0, src->size());
}

bool ByteBuffer::hasArray() {
    return (hb != nullptr) && !isReadOnly;
}

std::vector<char> *ByteBuffer::array() {
    if (hb == nullptr)
        throw java::lang::UnsupportedOperationException();

    if (isReadOnly)
        throw Exception(new std::string("ReadOnlyBufferException"));

    return hb;
}

int ByteBuffer::arrayOffset() {
    if (hb == nullptr)
        throw java::lang::UnsupportedOperationException();

    if (isReadOnly)
        throw Exception(new std::string("ReadOnlyBufferException"));

    return offset;
}

std::string *ByteBuffer::toString() {
    auto *sb = new StringBuffer();
    //sb->append(Buffer::getClass()->getName());
    sb->append(new std::string("ByteBuffer"));
    sb->append(new std::string("[pos="));
    sb->append(Buffer::position());
    sb->append(new std::string(" lim="));
    sb->append(Buffer::limit());
    sb->append(new std::string(" cap="));
    sb->append(Buffer::capacity());
    sb->append(new std::string("]"));
    return sb->toString();
}

int ByteBuffer::hashCode() {
    int h = 1;
    int p = Buffer::position();
    for (int i = Buffer::limit() - 1; i >= p; i--)
        h = 31 * h + (int) get(i);
    return h;
}

bool ByteBuffer::equals(java::lang::Object *ob) {
    if (this == ob)
        return true;

    if (!(instanceof<ByteBuffer>(ob)))
        return false;

    auto *that = (ByteBuffer *) ob;
    if (this->remaining() != that->remaining())
        return false;

    int p = this->position();
    for (int i = this->limit() - 1, j = that->limit() - 1; i >= p; i--, j--)
        if (!ByteBuffer::equals(this->get(i), that->get(j)))
            return false;

    return true;
}

bool ByteBuffer::equals(char x, char y) {
    return x == y;
}

int ByteBuffer::compareTo(ByteBuffer *that) {
    int n = this->position() + std::min(this->remaining(), that->remaining());
    for (int i = this->position(), j = that->position(); i < n; i++, j++) {
        int cmp = ByteBuffer::compare(this->get(i), that->get(j));
        if (cmp != 0)
            return cmp;

    }

    return this->remaining() - that->remaining();
}

int ByteBuffer::compare(char x, char y) {
    return x < y ? -1 : (x == y ? 0 : 1);
}

ByteOrder *ByteBuffer::order() {
    return bigEndian ? ByteOrder::BIG_ENDIAN_ : ByteOrder::LITTLE_ENDIAN_;
}

ByteBuffer *ByteBuffer::order(ByteOrder *bo) {
    bigEndian = (bo == ByteOrder::
    BIG_ENDIAN_);
    nativeByteOrder = (bigEndian == (Bits::byteOrder() == ByteOrder::
    BIG_ENDIAN_));
    return this;
}

