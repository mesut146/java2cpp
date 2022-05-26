#pragma once

namespace java {
    namespace lang {

        class Long {
            //fields
        public:
            static int BYTES;
            static long MAX_VALUE;
            static long MIN_VALUE;
            static int SIZE;
            long value;

            //methods
        public:
            Long(long);

            Long(std::string *);

            static int bitCount(long);

            char byteValue();

            static int compare(long, long);

            int compareTo(Long *);

            static int compareUnsigned(long, long);

            static Long *decode(std::string *);

            static long divideUnsigned(long, long);

            double doubleValue();

            float floatValue();

            static int formatUnsignedLong(long, int, std::vector<wchar_t> *, int, int);

            static void getChars(long, int, std::vector<wchar_t> *);

            static Long *getLong(std::string *);

            static Long *getLong(std::string *, long);

            static Long *getLong(std::string *, Long *);

            int hashCode();

            static int hashCode(long);

            static long highestOneBit(long);

            int intValue();

            long longValue();

            static long lowestOneBit(long);

            static long max(long, long);

            static long min(long, long);

            static int numberOfLeadingZeros(long);

            static int numberOfTrailingZeros(long);

            static long parseLong(std::string *);

            static long parseLong(std::string *, int);

            static long parseUnsignedLong(std::string *);

            static long parseUnsignedLong(std::string *, int);

            static long remainderUnsigned(long, long);

            static long reverse(long);

            static long reverseBytes(long);

            static long rotateLeft(long, int);

            static long rotateRight(long, int);

            char16_t shortValue();

            static int signum(long);

            static int stringSize(long);

            static long sum(long, long);

            static std::string *toBinaryString(long);

            static std::string *toHexString(long);

            static std::string *toOctalString(long);

            std::string *toString();

            static std::string *toString(long);

            static std::string *toString(long, int);

            static std::string *toUnsignedString(long);

            static std::string *toUnsignedString(long, int);

            static std::string *toUnsignedString0(long, int);

            static Long *valueOf(std::string *);

            static Long *valueOf(long);

            static Long *valueOf(std::string *, int);

        }; //class Long

    } //namespace java
} //namespace lang
