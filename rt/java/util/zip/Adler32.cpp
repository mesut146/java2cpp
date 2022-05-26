#include <cassert>
#include <stdexcept>
#include "java/lang/NullPointerException.h"
#include "java/lang/ArrayIndexOutOfBoundsException.h"
#include "java/nio/DirectBuffer.h"
#include "Adler32.h"
#include "CppHelper.h"

using namespace java::util::zip;
using namespace java::lang;

Adler32::Adler32() {

}

long Adler32::getValue() const {
    return (long) adler & 0xffffffffL;
}

void Adler32::update(int b) {
    adler = update(adler, b);
}

void Adler32::update(std::vector<char> *b, int off, int len) {
    if (b == nullptr) {
        throw NullPointerException();
    }
    if (off < 0 || len < 0 || off > b->size() - len) {
        throw ArrayIndexOutOfBoundsException();
    }
    adler = updateBytes(adler, b, off, len);
}

void Adler32::update(std::vector<char> *b) {
    adler = updateBytes(adler, b, 0, (int) b->size());
}

void Adler32::update(java::nio::ByteBuffer *buffer) {
    int pos = buffer->position();
    int limit = buffer->limit();
    assert(pos <= limit);
    int rem = limit - pos;
    if (rem <= 0)
        return;
    if (instanceof<nio::DirectBuffer>(buffer)) {
        adler = updateByteBuffer(adler, (dynamic_cast<nio::DirectBuffer *> (buffer))->address(), pos, rem);
    } else if (buffer->hasArray()) {
        adler = updateBytes(adler, buffer->array(), pos + buffer->arrayOffset(), rem);
    } else {
        auto b = new std::vector<char>(rem);
        buffer->get(b);
        adler = updateBytes(adler, b, 0, (int) b->size());
    }
    buffer->position(limit);
}

void Adler32::reset() {
    adler = 1;
}

void err(std::string &&m) {
    throw std::runtime_error(m);
}

//native
int Adler32::update(int adler, int b) {
    err("Adler32::update");
    return 0;
}

//native
int Adler32::updateByteBuffer(int adler, long addr, int off, int len) {
    err("Adler32::updateByteBuffer");
    return 0;
}

//native
int Adler32::updateBytes(int adler, std::vector<char> *b, int off, int len) {
    err("Adler32::updateBytes");
    return 0;
}
