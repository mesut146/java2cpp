#include "CharBuffer.h"
#include "java/lang/Exception.h"

using namespace java::nio;
using namespace java::lang;

CharBuffer *CharBuffer::put(std::vector<wchar_t> *src, int offset, int length) {
    checkBounds(offset, length, src->size());
    if (length > remaining())
        throw Exception("BufferOverflowException");
    int end = offset + length;
    for (int i = offset; i < end; i++)
        this->put((*src)[i]);
    return this;
}