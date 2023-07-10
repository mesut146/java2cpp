#pragma once

#include <string>
#include <vector>

namespace java {
    namespace lang {

        class Integer {
        public:
            constexpr static int MAX_VALUE = INT32_MAX;
            constexpr static int MIN_VALUE = INT32_MIN;
            int value;

            explicit Integer(int x);

            static Integer* make(int x);

            bool equals(Integer* other);

            //Integer(std::string *);

            int operator-(Integer &other);

            static int bitCount(int);

            char byteValue();

            static int compare(int, int);

            int compareTo(Integer *);

            static int compareUnsigned(int, int);

            static Integer *decode(std::string *);

            static int divideUnsigned(int, int);

            double doubleValue();

            float floatValue();

            static int formatUnsignedInt(int, int, std::vector<wchar_t> *, int, int);

            static void getChars(int, int, std::vector<wchar_t> *);

            static Integer *getInteger(std::string *);

            static Integer *getInteger(std::string *, int);

            static Integer *getInteger(std::string *, Integer *);

            int hashCode();

            static int hashCode(int);

            static int highestOneBit(int);

            int intValue();

            long longValue();

            static int lowestOneBit(int);

            static int max(int, int);

            static int min(int, int);

            static int numberOfLeadingZeros(int);

            static int numberOfTrailingZeros(int);

            static int parseInt(std::string *);

            static int parseInt(std::string *, int);

            static int parseUnsignedInt(std::string *);

            static int parseUnsignedInt(std::string *, int);

            static int remainderUnsigned(int, int);

            static int reverse(int);

            static int reverseBytes(int);

            static int rotateLeft(int, int);

            static int rotateRight(int, int);

            char16_t shortValue();

            static int signum(int);

            static int stringSize(int);

            static int sum(int, int);

            static std::string *toBinaryString(int);

            static std::string *toHexString(int);

            static std::string *toOctalString(int);

            std::string *toString();

            static std::string *toString(int);

            static std::string *toString(int, int);

            static long toUnsignedLong(int);

            static std::string *toUnsignedString(int);

            static std::string *toUnsignedString(int, int);

            static std::string *toUnsignedString0(int, int);

            static Integer *valueOf(std::string *);

            static Integer *valueOf(int);

            static Integer *valueOf(std::string *, int);

        }; //class Integer

    } //namespace java
} //namespace lang
