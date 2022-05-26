#include <java/lang/IndexOutOfBoundsException.h>
#include "java/io/DataInputStream.h"
#include "EOFException.h"
#include "CppHelper.h"
#include "UTFDataFormatException.h"
#include "java/lang/Float.h"
#include "java/lang/Double.h"

using namespace java::io;
using namespace java::lang;

//methods
DataInputStream::DataInputStream(InputStream *in) : FilterInputStream(in) {
}

int DataInputStream::read(std::vector<char> *b) {
    return in->read(b, 0, b->size());
}

int DataInputStream::read(std::vector<char> *b, int off, int len) {
    return in->read(b, off, len);
}

void DataInputStream::readFully(std::vector<char> *b) {
    readFully(b, 0, b->size());
}

void DataInputStream::readFully(std::vector<char> *b, int off, int len) {
    if (len < 0)
        throw new java::lang::IndexOutOfBoundsException();

    int n = 0;
    while (n < len) {
        int count = in->read(b, off + n, len - n);
        if (count < 0)
            throw new EOFException();
        n += count;
    }

}

int DataInputStream::skipBytes(int n) {
    int total = 0;
    int cur = 0;
    while ((total < n) && ((cur = (int) in->skip(n - total)) > 0)) {
        total += cur;
    }

    return total;
}

bool DataInputStream::readBoolean() {
    int ch = in->read();
    if (ch < 0)
        throw new EOFException();

    return (ch != 0);
}

char DataInputStream::readByte() {
    int ch = in->read();
    if (ch < 0)
        throw new EOFException();

    return (char) (ch);
}

int DataInputStream::readUnsignedByte() {
    int ch = in->read();
    if (ch < 0)
        throw new EOFException();

    return ch;
}

char16_t DataInputStream::readShort() {
    int ch1 = in->read();
    int ch2 = in->read();
    if ((ch1 | ch2) < 0)
        throw new EOFException();

    return (char16_t) ((ch1 << 8) + (ch2 << 0));
}

int DataInputStream::readUnsignedShort() {
    int ch1 = in->read();
    int ch2 = in->read();
    if ((ch1 | ch2) < 0)
        throw new EOFException();

    return (ch1 << 8) + (ch2 << 0);
}

wchar_t DataInputStream::readChar() {
    int ch1 = in->read();
    int ch2 = in->read();
    if ((ch1 | ch2) < 0)
        throw new EOFException();

    return (wchar_t) ((ch1 << 8) + (ch2 << 0));
}

int DataInputStream::readInt() {
    int ch1 = in->read();
    int ch2 = in->read();
    int ch3 = in->read();
    int ch4 = in->read();
    if ((ch1 | ch2 | ch3 | ch4) < 0)
        throw new EOFException();

    return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
}

long DataInputStream::readLong() {
    readFully(readBuffer, 0, 8);
    return (((long) readBuffer->at(0) << 56) + ((long) (readBuffer->at(1) & 255) << 48) +
            ((long) (readBuffer->at(2) & 255) << 40) + ((long) (readBuffer->at(3) & 255) << 32) +
            ((long) (readBuffer->at(4) & 255) << 24) + ((readBuffer->at(5) & 255) << 16) +
            ((readBuffer->at(6) & 255) << 8) + ((readBuffer->at(7) & 255) << 0));
}

float DataInputStream::readFloat() {
    return Float::intBitsToFloat(readInt());
}

double DataInputStream::readDouble() {
    return Double::longBitsToDouble(readLong());
}

std::string *DataInputStream::readLine() {
    auto buf = lineBuffer;
    if (buf == nullptr) {
        buf = lineBuffer = new std::vector<wchar_t>(128);
    }

    int room = buf->size();
    int offset = 0;
    int c;
    loop:
    if ((c == -1) && (offset == 0)) {
        return nullptr;
    }
    return makeString(buf, 0, offset);
    //todo
    //return String::copyValueOf(buf, 0, offset);
}

std::string *DataInputStream::readUTF() {
    return DataInputStream::readUTF(this);
}

std::string *DataInputStream::readUTF(DataInput *in) {
    int utflen = in->readUnsignedShort();
    std::vector<char> *bytearr = nullptr;
    std::vector<wchar_t> *chararr = nullptr;
    if (instanceof<DataInputStream>(in)) {
        DataInputStream *dis = (DataInputStream *) in;
        if (dis->bytearr->size() < utflen) {
            dis->bytearr = new std::vector<char>(utflen * 2);
            dis->chararr = new std::vector<wchar_t>(utflen * 2);
        }

        chararr = dis->chararr;
        bytearr = dis->bytearr;
    } else {
        bytearr = new std::vector<char>(utflen);
        chararr = new std::vector<wchar_t>(utflen);
    }

    int c, char2, char3;
    int count = 0;
    int chararr_count = 0;
    in->readFully(bytearr, 0, utflen);
    while (count < utflen) {
        c = (int) bytearr->at(count) & 0xff;
        if (c > 127)
            break;

        count++;
        chararr->at(chararr_count++) = (wchar_t) c;
    }

    while (count < utflen) {
        c = (int) bytearr->at(count) & 0xff;
        if (c >> 4 == 0 || c >> 4 == 1 || c >> 4 == 2 || c >> 4 == 3 || c >> 4 == 4 || c >> 4 == 5 || c >> 4 == 6 ||
            c >> 4 == 7) {
            count++;
            chararr->at(chararr_count++) = (wchar_t) c;
        } else if (c >> 4 == 12 || c >> 4 == 13) {
            count += 2;
            if (count > utflen)
                throw new UTFDataFormatException(new std::string("malformed input: partial character at end"));

            char2 = (int) bytearr->at(count - 1);
            if ((char2 & 0xC0) != 0x80)
                throw new UTFDataFormatException(new std::string("malformed input around byte ") + count);

            chararr->at(chararr_count++) = (wchar_t) (((c & 0x1F) << 6) | (char2 & 0x3F));
        } else if (c >> 4 == 14) {
            count += 3;
            if (count > utflen)
                throw new UTFDataFormatException(new std::string("malformed input: partial character at end"));

            char2 = (int) bytearr->at(count - 2);
            char3 = (int) bytearr->at(count - 1);
            if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                throw new UTFDataFormatException(new std::string("malformed input around byte ") + (count - 1));

            chararr->at(chararr_count++) = (wchar_t) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) |
                                                      ((char3 & 0x3F) << 0));
        } else {
            throw new UTFDataFormatException(new std::string("malformed input around byte ") + count);
        }

    }
    return makeString(chararr, 0, chararr_count);
}

