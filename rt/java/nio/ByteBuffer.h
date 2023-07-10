#pragma once

#include <vector>
#include <string>
#include "java/lang/Object.h"
#include "Buffer.h"
#include "ByteOrder.h"

namespace java {
    namespace nio {

        class ByteBuffer : public Buffer, public java::lang::Object {
            //fields
        public:
            bool bigEndian;
            std::vector<char> *hb;
            bool isReadOnly;
            bool nativeByteOrder;
            int offset;

            //methods
        public:
            ByteBuffer(int, int, int, int);

            ByteBuffer(int, int, int, int, std::vector<char> *, int);


            virtual char _get(int) = 0;

            virtual void _put(int, char) = 0;

            //static ByteBuffer *allocate(int);

            //static ByteBuffer *allocateDirect(int);

            std::vector<char> *array();

            int arrayOffset() override;

            /*virtual CharBuffer *asCharBuffer() = 0;

            virtual DoubleBuffer *asDoubleBuffer() = 0;

            virtual FloatBuffer *asFloatBuffer() = 0;

            virtual IntBuffer *asIntBuffer() = 0;

            virtual LongBuffer *asLongBuffer() = 0;

            virtual ByteBuffer *asReadOnlyBuffer() = 0;

            virtual ShortBuffer *asShortBuffer() = 0;*/

            virtual ByteBuffer *compact() = 0;

            static int compare(char, char);

            int compareTo(ByteBuffer *);

            virtual ByteBuffer *duplicate() = 0;

            bool equals(java::lang::Object *);

            static bool equals(char, char);

            virtual char get() = 0;

            virtual char get(int) = 0;

            ByteBuffer *get(std::vector<char> *);

            ByteBuffer *get(std::vector<char> *, int, int);

            virtual wchar_t getChar() = 0;

            virtual wchar_t getChar(int) = 0;

            virtual double getDouble() = 0;

            virtual double getDouble(int) = 0;

            virtual float getFloat() = 0;

            virtual float getFloat(int) = 0;

            virtual int getInt() = 0;

            virtual int getInt(int) = 0;

            virtual long getLong() = 0;

            virtual long getLong(int) = 0;

            virtual char16_t getShort() = 0;

            virtual char16_t getShort(int) = 0;

            bool hasArray() override;

            int hashCode() override;

            bool isDirect() override = 0;

            ByteOrder *order();

            ByteBuffer *order(ByteOrder *);

            virtual ByteBuffer *put(char) = 0;

            ByteBuffer *put(ByteBuffer *);

            ByteBuffer *put(std::vector<char> *);

            virtual ByteBuffer *put(int, char) = 0;

            ByteBuffer *put(std::vector<char> *, int, int);

            virtual ByteBuffer *putChar(wchar_t) = 0;

            virtual ByteBuffer *putChar(int, wchar_t) = 0;

            virtual ByteBuffer *putDouble(double) = 0;

            virtual ByteBuffer *putDouble(int, double) = 0;

            virtual ByteBuffer *putFloat(float) = 0;

            virtual ByteBuffer *putFloat(int, float) = 0;

            virtual ByteBuffer *putInt(int) = 0;

            virtual ByteBuffer *putInt(int, int) = 0;

            virtual ByteBuffer *putLong(long) = 0;

            virtual ByteBuffer *putLong(int, long) = 0;

            virtual ByteBuffer *putShort(char16_t) = 0;

            virtual ByteBuffer *putShort(int, char16_t) = 0;

            virtual ByteBuffer *slice() = 0;

            std::string *toString() override;

            static ByteBuffer *wrap(std::vector<char> *);

            static ByteBuffer *wrap(std::vector<char> *, int, int);

        }; //class ByteBuffer

    } //namespace java
} //namespace nio
