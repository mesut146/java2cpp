#include "java/lang/Float.h"
#include "java/lang/Double.h"
#include "java/lang/Exception.h"
#include "Bits.h"


using namespace java::nio;
using namespace java::lang;

//https://stackoverflow.com/a/1001373
bool is_big_endian() {
    union {
        uint32_t i;
        char c[4];
    } bint = {0x01020304};

    return bint.c[0] == 1;
}


ByteOrder *Bits::byteOrder_renamed = is_big_endian() ? ByteOrder::BIG_ENDIAN_ : ByteOrder::LITTLE_ENDIAN_;
ByteOrder *ByteOrder::LITTLE_ENDIAN_ = new ByteOrder("LITTLE_ENDIAN");
ByteOrder *ByteOrder::BIG_ENDIAN_ = new ByteOrder("BIG_ENDIAN");

java::nio::ByteOrder *Bits::byteOrder() {
    if (byteOrder_renamed == nullptr)
        throw new Exception(new std::string("Unknown byte order"));
    return byteOrder_renamed;
}

ByteOrder::ByteOrder(std::string &&name) : name(name) {}

std::string *ByteOrder::toString() {
    return &name;
}

wchar_t Bits::getChar(java::nio::ByteBuffer *bb, int bi, bool bigEndian) {
    return bigEndian ? getCharB(bb, bi) : getCharL(bb, bi);
}

wchar_t Bits::getCharB(java::nio::ByteBuffer *bb, int bi) {
    return makeChar(bb->_get(bi),
                    bb->_get(bi + 1));
}

wchar_t Bits::getCharL(java::nio::ByteBuffer *bb, int bi) {
    return makeChar(bb->_get(bi + 1),
                    bb->_get(bi));
}

wchar_t Bits::makeChar(char b1, char b0) {
    return (char) ((b1 << 8) | (b0 & 0xff));
}

double Bits::getDouble(ByteBuffer *bb, int bi, bool bigEndian) {
    return bigEndian ? getDoubleB(bb, bi) : getDoubleL(bb, bi);
}

double Bits::getDoubleB(ByteBuffer *bb, int bi) {
    return Double::longBitsToDouble(getLongB(bb, bi));
}

double Bits::getDoubleL(ByteBuffer *bb, int bi) {
    return Double::longBitsToDouble(getLongL(bb, bi));
}

long Bits::getLongB(ByteBuffer *bb, int bi) {
    return makeLong(bb->_get(bi),
                    bb->_get(bi + 1),
                    bb->_get(bi + 2),
                    bb->_get(bi + 3),
                    bb->_get(bi + 4),
                    bb->_get(bi + 5),
                    bb->_get(bi + 6),
                    bb->_get(bi + 7));
}

long Bits::getLongL(ByteBuffer *bb, int bi) {
    return makeLong(bb->_get(bi + 7),
                    bb->_get(bi + 6),
                    bb->_get(bi + 5),
                    bb->_get(bi + 4),
                    bb->_get(bi + 3),
                    bb->_get(bi + 2),
                    bb->_get(bi + 1),
                    bb->_get(bi));
}

long Bits::makeLong(char b7, char b6, char b5, char b4, char b3, char b2, char b1, char b0) {
    return ((((long) b7) << 56) |
            (((long) b6 & 0xff) << 48) |
            (((long) b5 & 0xff) << 40) |
            (((long) b4 & 0xff) << 32) |
            (((long) b3 & 0xff) << 24) |
            (((long) b2 & 0xff) << 16) |
            (((long) b1 & 0xff) << 8) |
            (((long) b0 & 0xff)));
}

float Bits::getFloat(ByteBuffer *bb, int bi, bool bigEndian) {
    return bigEndian ? getFloatB(bb, bi) : getFloatL(bb, bi);
}

int Bits::getInt(ByteBuffer *bb, int bi, bool bigEndian) {
    return bigEndian ? getIntB(bb, bi) : getIntL(bb, bi);
}

long Bits::getLong(ByteBuffer *bb, int bi, bool bigEndian) {
    return bigEndian ? getLongB(bb, bi) : getLongL(bb, bi);
}

char16_t Bits::getShort(ByteBuffer *bb, int bi, bool bigEndian) {
    return bigEndian ? getShortB(bb, bi) : getShortL(bb, bi);
}

float Bits::getFloatB(ByteBuffer *bb, int bi) {
    return Float::intBitsToFloat(getIntB(bb, bi));
}

float Bits::getFloatL(ByteBuffer *bb, int bi) {
    return Float::intBitsToFloat(getIntL(bb, bi));
}

int Bits::getIntB(ByteBuffer *bb, int bi) {
    return makeInt(bb->_get(bi),
                   bb->_get(bi + 1),
                   bb->_get(bi + 2),
                   bb->_get(bi + 3));
}

int Bits::getIntL(ByteBuffer *bb, int bi) {
    return makeInt(bb->_get(bi + 3),
                   bb->_get(bi + 2),
                   bb->_get(bi + 1),
                   bb->_get(bi));
}

int Bits::makeInt(char b3, char b2, char b1, char b0) {
    return (((b3) << 24) |
            ((b2 & 0xff) << 16) |
            ((b1 & 0xff) << 8) |
            ((b0 & 0xff)));
}

char16_t Bits::getShortB(ByteBuffer *bb, int bi) {
    return makeShort(bb->_get(bi),
                     bb->_get(bi + 1));
}

char16_t Bits::getShortL(ByteBuffer *bb, int bi) {
    return makeShort(bb->_get(bi + 1),
                     bb->_get(bi));
}

char16_t Bits::makeShort(char b1, char b0) {
    return (short) ((b1 << 8) | (b0 & 0xff));
}

void Bits::putChar(ByteBuffer *bb, int bi, wchar_t x, bool bigEndian) {
    if (bigEndian)
        putCharB(bb, bi, x);
    else
        putCharL(bb, bi, x);
}

static char char1(wchar_t x) { return (char) (x >> 8); }

static char char0(wchar_t x) { return (char) (x); }

void Bits::putCharB(ByteBuffer *bb, int bi, wchar_t x) {
    bb->_put(bi, char1(x));
    bb->_put(bi + 1, char0(x));
}

void Bits::putCharL(ByteBuffer *bb, int bi, wchar_t x) {
    bb->_put(bi, char0(x));
    bb->_put(bi + 1, char1(x));
}

void Bits::putInt(ByteBuffer *bb, int bi, int x, bool bigEndian) {
    if (bigEndian)
        putIntB(bb, bi, x);
    else
        putIntL(bb, bi, x);
}


static char int3(int x) { return (char) (x >> 24); }

static char int2(int x) { return (char) (x >> 16); }

static char int1(int x) { return (char) (x >> 8); }

static char int0(int x) { return (char) (x); }

void Bits::putIntB(ByteBuffer *bb, int bi, int x) {
    bb->_put(bi, int3(x));
    bb->_put(bi + 1, int2(x));
    bb->_put(bi + 2, int1(x));
    bb->_put(bi + 3, int0(x));
}

void Bits::putIntL(ByteBuffer *bb, int bi, int x) {
    bb->_put(bi + 3, int3(x));
    bb->_put(bi + 2, int2(x));
    bb->_put(bi + 1, int1(x));
    bb->_put(bi, int0(x));
}

void Bits::putLong(ByteBuffer *bb, int bi, long x, bool bigEndian) {
    if (bigEndian)
        putLongB(bb, bi, x);
    else
        putLongL(bb, bi, x);
}

static char long7(long x) { return (char) (x >> 56); }

static char long6(long x) { return (char) (x >> 48); }

static char long5(long x) { return (char) (x >> 40); }

static char long4(long x) { return (char) (x >> 32); }

static char long3(long x) { return (char) (x >> 24); }

static char long2(long x) { return (char) (x >> 16); }

static char long1(long x) { return (char) (x >> 8); }

static char long0(long x) { return (char) (x); }

void Bits::putLongB(ByteBuffer *bb, int bi, long x) {
    bb->_put(bi, long7(x));
    bb->_put(bi + 1, long6(x));
    bb->_put(bi + 2, long5(x));
    bb->_put(bi + 3, long4(x));
    bb->_put(bi + 4, long3(x));
    bb->_put(bi + 5, long2(x));
    bb->_put(bi + 6, long1(x));
    bb->_put(bi + 7, long0(x));
}

void Bits::putLongL(ByteBuffer *bb, int bi, long x) {
    bb->_put(bi + 7, long7(x));
    bb->_put(bi + 6, long6(x));
    bb->_put(bi + 5, long5(x));
    bb->_put(bi + 4, long4(x));
    bb->_put(bi + 3, long3(x));
    bb->_put(bi + 2, long2(x));
    bb->_put(bi + 1, long1(x));
    bb->_put(bi, long0(x));
}

void Bits::putShort(ByteBuffer *bb, int bi, char16_t x, bool bigEndian) {
    if (bigEndian)
        putShortB(bb, bi, x);
    else
        putShortL(bb, bi, x);
}

static char short1(char16_t x) { return (char) (x >> 8); }

static char short0(char16_t x) { return (char) (x); }

void Bits::putShortB(ByteBuffer *bb, int bi, char16_t x) {
    bb->_put(bi, short1(x));
    bb->_put(bi + 1, short0(x));
}

void Bits::putShortL(ByteBuffer *bb, int bi, char16_t x) {
    bb->_put(bi, short0(x));
    bb->_put(bi + 1, short1(x));
}

void Bits::putFloat(ByteBuffer *bb, int bi, float x, bool bigEndian) {
    if (bigEndian)
        putFloatB(bb, bi, x);
    else
        putFloatL(bb, bi, x);
}

void Bits::putFloatB(ByteBuffer *bb, int bi, float x) {
    putIntB(bb, bi, Float::floatToRawIntBits(x));
}

void Bits::putFloatL(ByteBuffer *bb, int bi, float x) {
    putIntL(bb, bi, Float::floatToRawIntBits(x));
}

void Bits::putDouble(ByteBuffer *bb, int bi, double x, bool bigEndian) {
    if (bigEndian)
        putDoubleB(bb, bi, x);
    else
        putDoubleL(bb, bi, x);
}

void Bits::putDoubleB(ByteBuffer *bb, int bi, double x) {
    putLongB(bb, bi, Double::doubleToRawLongBits(x));
}

void Bits::putDoubleL(ByteBuffer *bb, int bi, double x) {
    putLongL(bb, bi, Double::doubleToRawLongBits(x));
}
