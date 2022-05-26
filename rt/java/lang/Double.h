#pragma once

#include <string>

namespace java {
    namespace lang {

        class Double {
            //fields
        public:
            static int BYTES;
            static int MAX_EXPONENT;
            static double MAX_VALUE;
            static int MIN_EXPONENT;
            static double MIN_NORMAL;
            static double MIN_VALUE;
            static double NEGATIVE_INFINITY;
            static double NaN;
            static double POSITIVE_INFINITY;
            static int SIZE;
            static long serialVersionUID;
            double value;

            char byteValue();

            static int compare(double, double);

            int compareTo(Double *);

            static long doubleToLongBits(double);

            static long doubleToRawLongBits(double);

            double doubleValue();

            float floatValue();

            int hashCode();

            static int hashCode(double);

            int intValue();

            static bool isFinite(double);

            bool isInfinite();

            static bool isInfinite(double);

            bool isNaN();

            static bool isNaN(double);

            static double longBitsToDouble(long);

            long longValue();

            static double max(double, double);

            static double min(double, double);

            static double parseDouble(std::string *);

            char16_t shortValue();

            static double sum(double, double);

            static std::string *toHexString(double);

            std::string *toString();

            static std::string *toString(double);

            static Double *valueOf(std::string *);

            static Double *valueOf(double);

        }; //class Double

    } //namespace java
} //namespace lang
