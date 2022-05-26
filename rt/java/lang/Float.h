#pragma once

namespace java {
    namespace lang {

        class Float {
            //fields
        public:
            static int BYTES;
            static int MAX_EXPONENT;
            static float MAX_VALUE;
            static int MIN_EXPONENT;
            static float MIN_NORMAL;
            static float MIN_VALUE;
            static float NEGATIVE_INFINITY;
            static float NaN;
            static float POSITIVE_INFINITY;
            static int SIZE;
            static long serialVersionUID;
            float value;

            //methods
        public:
            Float(float);

            Float(double);

            //Float(std::string *);

            char byteValue();

            static int compare(float, float);

            int compareTo(Float *);

            double doubleValue();

            //bool equals(Object *);

            static int floatToIntBits(float);

            static int floatToRawIntBits(float);

            float floatValue();

            int hashCode();

            static int hashCode(float);

            static float intBitsToFloat(int);

            int intValue();

            static bool isFinite(float);

            bool isInfinite();

            static bool isInfinite(float);

            bool isNaN();

            static bool isNaN(float);

            long longValue();

            static float max(float, float);

            static float min(float, float);

            //static float parseFloat(std::string *);

            char16_t shortValue();

            static float sum(float, float);

            //static std::string *toHexString(float);

            //std::string *toString();

            //static std::string *toString(float);

            //static Float *valueOf(std::string *);

            //static Float *valueOf(float);

        }; //class Float

    } //namespace java
} //namespace lang
