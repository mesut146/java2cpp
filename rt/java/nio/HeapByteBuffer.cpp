#include "HeapByteBuffer.h"
#include "Bits.h"
#include "CppHelper.h"

using namespace java::nio;


HeapByteBuffer::HeapByteBuffer(int cap, int lim) : ByteBuffer(-1, 0, lim, cap, new std::vector<char>(10), 0) {}

HeapByteBuffer::HeapByteBuffer(std::vector<char> *buf, int off, int len) : ByteBuffer(-1, off, off + len, buf->size(),
                                                                                      buf, 0) {

}

HeapByteBuffer::HeapByteBuffer(std::vector<char> *buf, int mark, int pos, int lim, int cap, int off) : ByteBuffer(mark,
                                                                                                                  pos,
                                                                                                                  lim,
                                                                                                                  cap,
                                                                                                                  buf,
                                                                                                                  off) {

}

bool HeapByteBuffer::isDirect() {
    return false;
}

bool HeapByteBuffer::isReadOnly() {
    return false;
}

char HeapByteBuffer::get() {
    return (*hb)[ix(nextGetIndex())];
}

int HeapByteBuffer::ix(int i) const {
    return i + offset;
}

char HeapByteBuffer::get(int i) {
    return (*hb)[ix(checkIndex(i))];
}

char HeapByteBuffer::_get(int i) {
    return (*hb)[i];
}

void HeapByteBuffer::_put(int i, char c) {
    hb->at(i) = c;
}

ByteBuffer *HeapByteBuffer::compact() {
    arraycopy(hb, ix(position()), hb, ix(0), remaining());
    position(remaining());
    limit(capacity());
    discardMark();
    return this;
}

ByteBuffer *HeapByteBuffer::duplicate() {
    return new HeapByteBuffer(hb,
                              this->markValue(),
                              this->position(),
                              this->limit(),
                              this->capacity(),
                              offset);
}

wchar_t HeapByteBuffer::getChar() {
    return Bits::getChar(this, ix(nextGetIndex(2)), bigEndian);
}

wchar_t HeapByteBuffer::getChar(int i) {
    return Bits::getChar(this, ix(checkIndex(i, 2)), bigEndian);
}

double HeapByteBuffer::getDouble() {
    return Bits::getDouble(this, ix(nextGetIndex(8)), bigEndian);
}

double HeapByteBuffer::getDouble(int i) {
    return Bits::getDouble(this, ix(checkIndex(i, 8)), bigEndian);
}

float HeapByteBuffer::getFloat() {
    return Bits::getFloat(this, ix(nextGetIndex(4)), bigEndian);
}

float HeapByteBuffer::getFloat(int i) {
    return Bits::getFloat(this, ix(checkIndex(i, 4)), bigEndian);
}

int HeapByteBuffer::getInt() {
    return Bits::getInt(this, ix(nextGetIndex(4)), bigEndian);
}

int HeapByteBuffer::getInt(int i) {
    return Bits::getInt(this, ix(checkIndex(i, 4)), bigEndian);
}

long HeapByteBuffer::getLong() {
    return Bits::getLong(this, ix(nextGetIndex(8)), bigEndian);
}

long HeapByteBuffer::getLong(int i) {
    return Bits::getLong(this, ix(checkIndex(i, 8)), bigEndian);
}

char16_t HeapByteBuffer::getShort() {
    return Bits::getShort(this, ix(nextGetIndex(2)), bigEndian);
}

char16_t HeapByteBuffer::getShort(int i) {
    return Bits::getShort(this, ix(checkIndex(i, 2)), bigEndian);
}

ByteBuffer *HeapByteBuffer::put(char x) {
    (*hb)[ix(nextPutIndex())] = x;
    return this;
}

ByteBuffer *HeapByteBuffer::put(int i, char x) {
    (*hb)[ix(checkIndex(i))] = x;
    return this;
}

ByteBuffer *HeapByteBuffer::putChar(wchar_t x) {
    Bits::putChar(this, ix(nextPutIndex(2)), x, bigEndian);
    return this;
}

ByteBuffer *HeapByteBuffer::putChar(int i, wchar_t x) {
    Bits::putChar(this, ix(checkIndex(i, 2)), x, bigEndian);
    return this;
}

ByteBuffer *HeapByteBuffer::slice() {
    return new HeapByteBuffer(hb,
                              -1,
                              0,
                              this->remaining(),
                              this->remaining(),
                              this->position() + offset);
}

ByteBuffer *HeapByteBuffer::putInt(int x) {
    Bits::putInt(this, ix(nextPutIndex(4)), x, bigEndian);
    return this;
}

ByteBuffer *HeapByteBuffer::putInt(int i, int x) {
    Bits::putInt(this, ix(checkIndex(i, 4)), x, bigEndian);
    return this;
}

ByteBuffer *HeapByteBuffer::putLong(long x) {
    Bits::putLong(this, ix(nextPutIndex(8)), x, bigEndian);
    return this;
}

ByteBuffer *HeapByteBuffer::putLong(int i, long x) {
    Bits::putLong(this, ix(checkIndex(i, 8)), x, bigEndian);
    return this;
}

ByteBuffer *HeapByteBuffer::putShort(char16_t x) {
    Bits::putShort(this, ix(nextPutIndex(2)), x, bigEndian);
    return this;
}

ByteBuffer *HeapByteBuffer::putShort(int i, char16_t x) {
    Bits::putShort(this, ix(checkIndex(i, 2)), x, bigEndian);
    return this;
}

ByteBuffer *HeapByteBuffer::putFloat(float x) {
    Bits::putFloat(this, ix(nextPutIndex(4)), x, bigEndian);
    return this;
}

ByteBuffer *HeapByteBuffer::putFloat(int i, float x) {
    Bits::putFloat(this, ix(checkIndex(i, 4)), x, bigEndian);
    return this;
}

ByteBuffer *HeapByteBuffer::putDouble(double x) {
    Bits::putDouble(this, ix(nextPutIndex(8)), x, bigEndian);
    return this;
}

ByteBuffer *HeapByteBuffer::putDouble(int i, double x) {
    Bits::putDouble(this, ix(checkIndex(i, 8)), x, bigEndian);
    return this;
}
