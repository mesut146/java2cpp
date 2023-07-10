#include "Double.h"
#include "Float.h"
#include <limits>
#include <cmath>

using namespace java::lang;

class DoubleConsts {
public:
    static const long EXP_BIT_MASK = 0x7FF0000000000000L;
    static const long SIGNIF_BIT_MASK = 0x000FFFFFFFFFFFFFL;
};

long Double::doubleToLongBits(double value) {
    long result = doubleToRawLongBits(value);
    // Check for NaN based on values of bit fields, maximum
    // exponent and nonzero significand.
    if (((result & DoubleConsts::EXP_BIT_MASK) ==
         DoubleConsts::EXP_BIT_MASK) &&
        (result & DoubleConsts::SIGNIF_BIT_MASK) != 0L)
        result = 0x7ff8000000000000L;
    return result;
}

double Double::longBitsToDouble(long bits) {
    if (bits == 0x7ff0000000000000L) {
        return std::numeric_limits<double>::infinity();
    }
    if (bits == 0xfff0000000000000L) {
        return -std::numeric_limits<double>::infinity();
    }
    if (bits >= 0x7ff0000000000001L && bits <= 0x7fffffffffffffffL ||
        bits >= 0xfff0000000000001L && bits <= 0xffffffffffffffffL) {
        return std::numeric_limits<double>::quiet_NaN();
    }
    int s = ((bits >> 63) == 0) ? 1 : -1;
    int e = (int) ((bits >> 52) & 0x7ffL);
    long m = (e == 0) ?
             (bits & 0xfffffffffffffL) << 1 :
             (bits & 0xfffffffffffffL) | 0x10000000000000L;
    return s * m * std::pow(2, e - 1075);
    //return (double) bits;
}

long Double::doubleToRawLongBits(double val) {
    return val;
}

int Float::floatToIntBits(float f) {
    return f;
}

float Float::intBitsToFloat(int i) {
    return i;
}

int Float::floatToRawIntBits(float f) {
    return f;
}

bool Float::isNaN(float v) {
    return (v != v);
}