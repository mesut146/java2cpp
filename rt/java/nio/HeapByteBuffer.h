#pragma once

#include "ByteBuffer.h"

namespace java {
    namespace nio {
        class HeapByteBuffer : public ByteBuffer {
            std::vector<char> *hb;
            int offset;
        public:
            HeapByteBuffer(std::vector<char> *buf, int off, int len);

            HeapByteBuffer(std::vector<char> *buf, int mark, int pos, int lim, int cap, int off);

            HeapByteBuffer(int cap, int lim);

            bool isDirect() override;

            bool isReadOnly() override;

            char get() override;

            char get(int i) override;

            int ix(int i) const;

            char _get(int i) override;

            void _put(int i, char c) override;

            ByteBuffer *compact() override;

            ByteBuffer *duplicate();

            wchar_t getChar(int i);

            wchar_t getChar();

            double getDouble();

            float getFloat();

            double getDouble(int i);

            float getFloat(int i);

            int getInt();

            int getInt(int i);

            long getLong();

            long getLong(int i);

            char16_t getShort();

            char16_t getShort(int i);

            ByteBuffer *slice();

            ByteBuffer *put(char);

            ByteBuffer *put(int, char);

            ByteBuffer *putChar(wchar_t);

            ByteBuffer *putChar(int, wchar_t);

            ByteBuffer *putInt(int);

            ByteBuffer *putInt(int, int);

            ByteBuffer *putLong(long);

            ByteBuffer *putLong(int, long);

            ByteBuffer *putShort(char16_t);

            ByteBuffer *putShort(int, char16_t);

            ByteBuffer *putFloat(float);

            ByteBuffer *putFloat(int, float);

            ByteBuffer *putDouble(double);

            ByteBuffer *putDouble(int, double);
        };
    }
}



