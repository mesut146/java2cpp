#include <cstdint>
#include "Integer.h"
#include "Long.h"
#include "CppHelper.h"

using namespace java::lang;

static int sizeTable[] = {9, 99, 999, 9999, 99999, 999999, 9999999,
                          99999999, 999999999, INT32_MAX};

static char DigitOnes[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
};

static char DigitTens[] = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
};

static char digits[] = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z'
};

int Integer::stringSize(int x) {
    for (int i = 0;; i++)
        if (x <= sizeTable[i])
            return i + 1;
}

int Integer::bitCount(int i) {
    // HD, Figure 5-2
    i = i - (((unsigned int) i >> 1) & 0x55555555);
    i = (i & 0x33333333) + (((unsigned int) i >> 2) & 0x33333333);
    i = (i + ((unsigned int) i >> 4)) & 0x0f0f0f0f;
    i = i + ((unsigned int) i >> 8);
    i = i + ((unsigned int) i >> 16);
    return i & 0x3f;
}

std::string *Integer::toHexString(int i) {
    return toUnsignedString(i, 4);
}

std::string *Integer::toUnsignedString(int i, int shift) {
    auto *buf = new std::vector<wchar_t>(32);
    int charPos = 32;
    int radix = 1 << shift;
    int mask = radix - 1;
    do {
        buf->at(--charPos) = digits[i & mask];
        i >>= shift;
    } while (i != 0);
    return makeString(buf, charPos, (32 - charPos));
}

int Integer::numberOfTrailingZeros(int i) {
    // HD, Figure 5-14
    int y;
    if (i == 0) return 32;
    int n = 31;
    y = i << 16;
    if (y != 0) {
        n = n - 16;
        i = y;
    }
    y = i << 8;
    if (y != 0) {
        n = n - 8;
        i = y;
    }
    y = i << 4;
    if (y != 0) {
        n = n - 4;
        i = y;
    }
    y = i << 2;
    if (y != 0) {
        n = n - 2;
        i = y;
    }
    return n - (((unsigned int) (i << 1)) >> 31);
}

int Integer::compare(int x, int y) {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
}

void Integer::getChars(int i, int index, std::vector<wchar_t> *buf) {
    int q, r;
    int charPos = index;
    char sign = 0;

    if (i < 0) {
        sign = '-';
        i = -i;
    }

    // Generate two digits per iteration
    while (i >= 65536) {
        q = i / 100;
        // really: r = i - (q * 100);
        r = i - ((q << 6) + (q << 5) + (q << 2));
        i = q;
        (*buf)[--charPos] = DigitOnes[r];
        (*buf)[--charPos] = DigitTens[r];
    }

    // Fall thru to fast mode for smaller numbers
    // assert(i <= 65536, i);
    for (;;) {
        q = (i * 52429) >> (16 + 3);
        r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
        (*buf)[--charPos] = digits[r];
        i = q;
        if (i == 0) break;
    }
    if (sign != 0) {
        (*buf)[--charPos] = sign;
    }
}

Integer::Integer(int x) {
    value = x;
}

int Integer::operator-(Integer &other) {
    return value - other.value;
}

int Integer::highestOneBit(int i) {
    // HD, Figure 3-1
    i |= (i >> 1);
    i |= (i >> 2);
    i |= (i >> 4);
    i |= (i >> 8);
    i |= (i >> 16);
    return i - ((unsigned int) i >> 1);
}

Integer *Integer::make(int x) {
    return new Integer(x);
}

bool Integer::equals(Integer *other) {
    return value == other->value;
}

int Integer::hashCode() {
    return value;
}

//-------------------
int Long::stringSize(long x) {
    long p = 10;
    for (int i = 1; i < 19; i++) {
        if (x < p)
            return i;
        p = 10 * p;
    }
    return 19;
}

void Long::getChars(long i, int index, std::vector<wchar_t> *buf) {
    long q;
    int r;
    int charPos = index;
    char sign = 0;

    if (i < 0) {
        sign = '-';
        i = -i;
    }

    // Get 2 digits/iteration using longs until quotient fits into an int
    while (i > Integer::MAX_VALUE) {
        q = i / 100;
        // really: r = i - (q * 100);
        r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
        i = q;
        (*buf)[--charPos] = DigitOnes[r];
        (*buf)[--charPos] = DigitTens[r];
    }

    // Get 2 digits/iteration using ints
    int q2;
    int i2 = (int) i;
    while (i2 >= 65536) {
        q2 = i2 / 100;
        // really: r = i2 - (q * 100);
        r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
        i2 = q2;
        (*buf)[--charPos] = DigitOnes[r];
        (*buf)[--charPos] = DigitTens[r];
    }

    // Fall thru to fast mode for smaller numbers
    // assert(i2 <= 65536, i2);
    for (;;) {
        q2 = (i2 * 52429) >> (16 + 3);
        r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
        (*buf)[--charPos] = digits[r];
        i2 = q2;
        if (i2 == 0) break;
    }
    if (sign != 0) {
        (*buf)[--charPos] = sign;
    }
}

int Long::numberOfTrailingZeros(long i) {
    // HD, Figure 5-14
    int x, y;
    if (i == 0) return 64;
    int n = 63;
    y = (int) i;
    if (y != 0) {
        n = n - 32;
        x = y;
    } else x = (int) ((unsigned long) i >> 32);
    y = x << 16;
    if (y != 0) {
        n = n - 16;
        x = y;
    }
    y = x << 8;
    if (y != 0) {
        n = n - 8;
        x = y;
    }
    y = x << 4;
    if (y != 0) {
        n = n - 4;
        x = y;
    }
    y = x << 2;
    if (y != 0) {
        n = n - 2;
        x = y;
    }
    return n - ((unsigned long) (x << 1) >> 31);
}

int Long::bitCount(long i) {
    // HD, Figure 5-14
    i = i - (((unsigned int) i >> 1) & 0x5555555555555555L);
    i = (i & 0x3333333333333333L) + ((i >> 2) & 0x3333333333333333L);
    i = (i + ((unsigned long) i >> 4)) & 0x0f0f0f0f0f0f0f0fL;
    i = i + ((unsigned long) i >> 8);
    i = i + ((unsigned long) i >> 16);
    i = i + ((unsigned long) i >> 32);
    return (int) i & 0x7f;
}

int Long::numberOfLeadingZeros(long i) {
    // HD, Figure 5-6
    if (i == 0)
        return 64;
    int n = 1;
    int x = (int) ((unsigned long) i >> 32);
    if (x == 0) {
        n += 32;
        x = (int) i;
    }
    if ((unsigned long) x >> 16 == 0) {
        n += 16;
        x <<= 16;
    }
    if ((unsigned long) x >> 24 == 0) {
        n += 8;
        x <<= 8;
    }
    if ((unsigned long) x >> 28 == 0) {
        n += 4;
        x <<= 4;
    }
    if ((unsigned long) x >> 30 == 0) {
        n += 2;
        x <<= 2;
    }
    n -= (unsigned long) x >> 31;
    return n;
}