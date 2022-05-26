#pragma once

#include "ByteOrder.h"
#include "HeapByteBuffer.h"

namespace java {
    namespace nio {
        class Bits {
        public:
            static ByteOrder *byteOrder_renamed;

            static ByteOrder *byteOrder();

            static wchar_t getChar(java::nio::ByteBuffer *buff, int ix, bool endian);

            static wchar_t getCharB(java::nio::ByteBuffer *bb, int bi);

            static wchar_t getCharL(java::nio::ByteBuffer *bb, int bi);

            static wchar_t makeChar(char get, char get1);

            static double getDouble(ByteBuffer *bb, int bi, bool bigEndian);

            static double getDoubleB(ByteBuffer *bb, int bi);

            static double getDoubleL(ByteBuffer *bb, int bi);

            static long getLongB(ByteBuffer *bb, int bi);

            static long getLongL(ByteBuffer *bb, int bi);

            static long makeLong(char b7, char b6, char b5, char b4, char b3, char b2, char b1, char b0);

            static float getFloat(ByteBuffer *bb, int ix, bool endian);

            static int getInt(ByteBuffer *bb, int ix, bool endian);

            static long getLong(ByteBuffer *bb, int ix, bool endian);

            static char16_t getShort(ByteBuffer *bb, int ix, bool endian);

            static float getFloatB(ByteBuffer *bb, int bi);

            static float getFloatL(ByteBuffer *bb, int bi);

            static int getIntB(ByteBuffer *bb, int bi);

            static int getIntL(ByteBuffer *bb, int bi);

            static int makeInt(char b3, char b2, char b1, char b0);

            static char16_t getShortB(ByteBuffer *bb, int bi);

            static char16_t getShortL(ByteBuffer *bb, int bi);

            static char16_t makeShort(char b1, char b0);

            static void putChar(ByteBuffer *bb, int bi, wchar_t x, bool endian);

            static void putCharB(ByteBuffer *bb, int bi, wchar_t x);

            static void putCharL(ByteBuffer *bb, int bi, wchar_t x);

            static void putInt(ByteBuffer *bb, int bi, int x, bool endian);

            static void putIntB(ByteBuffer *bb, int bi, int x);

            static void putIntL(ByteBuffer *bb, int bi, int x);

            static void putLong(ByteBuffer *bb, int bi, long x, bool endian);

            static void putLongB(ByteBuffer *bb, int bi, long x);

            static void putLongL(ByteBuffer *bb, int bi, long x);

            static void putShort(ByteBuffer *bb, int bi, char16_t x, bool endian);

            static void putShortB(ByteBuffer *bb, int bi, char16_t x);

            static void putShortL(ByteBuffer *bb, int bi, char16_t x);

            static void putFloat(ByteBuffer *bb, int bi, float x, bool endian);

            static void putFloatB(ByteBuffer *bb, int bi, float x);

            static void putFloatL(ByteBuffer *bb, int bi, float x);

            static void putDouble(ByteBuffer *bb, int bi, double x, bool endian);

            static void putDoubleB(ByteBuffer *bb, int bi, double x);

            static void putDoubleL(ByteBuffer *bb, int bi, double x);
        };
    }
}

