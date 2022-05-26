#include "java/lang/IndexOutOfBoundsException.h"
#include "java/lang/IllegalArgumentException.h"
#include "BufferOverflowException.h"
#include "Buffer.h"

using namespace java::nio;
using namespace java::lang;


int Buffer::limit() {
    return limit_renamed;
}

int Buffer::capacity() {
    return capacity_renamed;
}

int Buffer::position() {
    return position_renamed;
}

Buffer *Buffer::mark() {
    mark_renamed = position_renamed;
    return this;
}

Buffer *Buffer::limit(int newLimit) {
    if ((newLimit > capacity_renamed) || (newLimit < 0))
        throw IllegalArgumentException();
    limit_renamed = newLimit;
    if (position_renamed > limit_renamed) position_renamed = limit_renamed;
    if (mark_renamed > limit_renamed) mark_renamed = -1;
    return this;
}

int Buffer::remaining() {
    return limit_renamed - position_renamed;
}

int Buffer::nextGetIndex() {
    if (position_renamed >= limit_renamed)
        throw Exception(new std::string("BufferUnderflowException"));
    return position_renamed++;
}

int Buffer::nextGetIndex(int nb) {
    if (limit_renamed - position_renamed < nb)
        throw Exception(new std::string("BufferUnderflowException"));
    int p = position_renamed;
    position_renamed += nb;
    return p;
}

int Buffer::checkIndex(int i) {
    if ((i < 0) || (i >= limit_renamed))
        throw IndexOutOfBoundsException();
    return i;
}

int Buffer::checkIndex(int i, int nb) {
    if ((i < 0) || (nb > limit_renamed - i))
        throw IndexOutOfBoundsException();
    return i;
}

Buffer *Buffer::position(int newPosition) {
    if ((newPosition > limit_renamed) || (newPosition < 0))
        throw IllegalArgumentException();
    position_renamed = newPosition;
    if (mark_renamed > position_renamed) mark_renamed = -1;
    return this;
}


void Buffer::discardMark() {
    mark_renamed = -1;
}

int Buffer::markValue() {
    return mark_renamed;
}

void Buffer::checkBounds(int off, int len, int size) {
    if ((off | len | (off + len) | (size - (off + len))) < 0)
        throw IndexOutOfBoundsException();
}

Buffer::Buffer(int mark, int pos, int lim, int cap) {
    if (cap < 0)
        throw IllegalArgumentException(new std::string("Negative capacity: " + std::to_string(cap)));
    this->capacity_renamed = cap;
    limit(lim);
    position(pos);
    if (mark >= 0) {
        if (mark > pos)
            throw IllegalArgumentException(new std::string("mark > position: ("
                                                           + std::to_string(mark) + " > " + std::to_string(pos) +
                                                           ")"));
        this->mark_renamed = mark;
    }
}

bool Buffer::hasRemaining() {
    return position_renamed < limit_renamed;
}

Buffer *Buffer::clear() {
    position_renamed = 0;
    limit_renamed = capacity_renamed;
    mark_renamed = -1;
    return this;
}

int Buffer::nextPutIndex() {
    if (position_renamed >= limit_renamed)
        throw BufferOverflowException();
    return position_renamed++;
}

int Buffer::nextPutIndex(int nb) {
    if (limit_renamed - position_renamed < nb)
        throw BufferOverflowException();
    int p = position_renamed;
    position_renamed += nb;
    return p;
}
