#include "Character.h"
#include "Exception.h"
#include "CharacterDataLatin1.h"

using namespace java::lang;

int Character::digit(wchar_t ch, int radix) {
    return digit((int) ch, radix);
}


int Character::digit(int ch, int radix) {
    if (ch >> 8 == 0) {     // fast-path
        return CharacterDataLatin1::digit(ch, radix);
    } else {
        throw std::invalid_argument("invalid code point: " + std::to_string(ch));
        /*switch (ch >> 16) {  //plane 00-16
            case (0):
                //return CharacterData00.instance;
            case (1):
                //return CharacterData01.instance;
            case (2):
                //return CharacterData02.instance;
            case (14):
                //return CharacterData0E.instance;
            case (15):   // Private Use
            case (16):   // Private Use
                //return CharacterDataPrivateUse.instance;
            default:
                //return CharacterDataUndefined.instance;
        }*/
    }
}

wchar_t Character::forDigit(int digit, int radix) {
    if ((digit >= radix) || (digit < 0)) {
        return '\0';
    }
    if ((radix < MIN_RADIX) || (radix > MAX_RADIX)) {
        return '\0';
    }
    if (digit < 10) {
        return (char) ('0' + digit);
    }
    return (char) ('a' - 10 + digit);
}
