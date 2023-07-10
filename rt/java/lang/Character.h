#pragma once

namespace java {
    namespace lang {

        class Character {
            //fields
        public:
            static int BYTES;
            static char COMBINING_SPACING_MARK;
            static char CONNECTOR_PUNCTUATION;
            static char CONTROL;
            static char CURRENCY_SYMBOL;
            static char DASH_PUNCTUATION;
            static constexpr char DECIMAL_DIGIT_NUMBER = 9;
            static char DIRECTIONALITY_ARABIC_NUMBER;
            static char DIRECTIONALITY_BOUNDARY_NEUTRAL;
            static char DIRECTIONALITY_COMMON_NUMBER_SEPARATOR;
            static char DIRECTIONALITY_EUROPEAN_NUMBER;
            static char DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR;
            static char DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR;
            static char DIRECTIONALITY_LEFT_TO_RIGHT;
            static char DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING;
            static char DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE;
            static char DIRECTIONALITY_NONSPACING_MARK;
            static char DIRECTIONALITY_OTHER_NEUTRALS;
            static char DIRECTIONALITY_PARAGRAPH_SEPARATOR;
            static char DIRECTIONALITY_POP_DIRECTIONAL_FORMAT;
            static char DIRECTIONALITY_RIGHT_TO_LEFT;
            static char DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
            static char DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING;
            static char DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE;
            static char DIRECTIONALITY_SEGMENT_SEPARATOR;
            static char DIRECTIONALITY_UNDEFINED;
            static char DIRECTIONALITY_WHITESPACE;
            static char ENCLOSING_MARK;
            static char END_PUNCTUATION;
            static int ERROR;
            static char FINAL_QUOTE_PUNCTUATION;
            static char FORMAT;
            static char INITIAL_QUOTE_PUNCTUATION;
            static char LETTER_NUMBER;
            static char LINE_SEPARATOR;
            static char LOWERCASE_LETTER;
            static char MATH_SYMBOL;
            static int MAX_CODE_POINT;
            static wchar_t MAX_HIGH_SURROGATE;
            static wchar_t MAX_LOW_SURROGATE;
            static constexpr int MAX_RADIX = 36;
            static wchar_t MAX_SURROGATE;
            static wchar_t MAX_VALUE;
            static int MIN_CODE_POINT;
            static wchar_t MIN_HIGH_SURROGATE;
            static wchar_t MIN_LOW_SURROGATE;
            static constexpr int MIN_RADIX = 2;
            static int MIN_SUPPLEMENTARY_CODE_POINT;
            static wchar_t MIN_SURROGATE;
            static wchar_t MIN_VALUE;
            static char MODIFIER_LETTER;
            static char MODIFIER_SYMBOL;
            static char NON_SPACING_MARK;
            static char OTHER_LETTER;
            static char OTHER_NUMBER;
            static char OTHER_PUNCTUATION;
            static char OTHER_SYMBOL;
            static char PARAGRAPH_SEPARATOR;
            static char PRIVATE_USE;
            static int SIZE;
            static char SPACE_SEPARATOR;
            static char START_PUNCTUATION;
            static char SURROGATE;
            static char TITLECASE_LETTER;
            static char UNASSIGNED;
            static char UPPERCASE_LETTER;
            static long serialVersionUID;
            wchar_t value;

            //methods
        public:
            Character(wchar_t);

            static int charCount(int);

            wchar_t charValue();


            /*static int codePointAt(std::vector<wchar_t> *, int);

            static int codePointAt(std::vector<wchar_t> *, int, int);

            static int codePointAtImpl(std::vector<wchar_t> *, int, int);

            static int codePointBefore(std::vector<wchar_t> *, int);

            static int codePointBefore(std::vector<wchar_t> *, int, int);

            static int codePointBeforeImpl(std::vector<wchar_t> *, int, int);

            static int codePointCount(std::vector<wchar_t> *, int, int);

            static int codePointCountImpl(std::vector<wchar_t> *, int, int);*/

            static int compare(wchar_t, wchar_t);

            int compareTo(Character *);

            static int digit(wchar_t, int);

            static int digit(int, int);


            static wchar_t forDigit(int, int);

            static char getDirectionality(wchar_t);

            static char getDirectionality(int);

            //static std::string *getName(int);

            static int getNumericValue(wchar_t);

            static int getNumericValue(int);

            static int getType(wchar_t);

            static int getType(int);

            int hashCode();

            static int hashCode(wchar_t);

            static wchar_t highSurrogate(int);

            static bool isAlphabetic(int);

            static bool isBmpCodePoint(int);

            static bool isDefined(wchar_t);

            static bool isDefined(int);

            static bool isDigit(wchar_t);

            static bool isDigit(int);

            static bool isHighSurrogate(wchar_t);

            static bool isISOControl(wchar_t);

            static bool isISOControl(int);

            static bool isIdentifierIgnorable(wchar_t);

            static bool isIdentifierIgnorable(int);

            static bool isIdeographic(int);

            static bool isJavaIdentifierPart(wchar_t);

            static bool isJavaIdentifierPart(int);

            static bool isJavaIdentifierStart(wchar_t);

            static bool isJavaIdentifierStart(int);

            static bool isJavaLetter(wchar_t);

            static bool isJavaLetterOrDigit(wchar_t);

            static bool isLetter(wchar_t);

            static bool isLetter(int);

            static bool isLetterOrDigit(wchar_t);

            static bool isLetterOrDigit(int);

            static bool isLowSurrogate(wchar_t);

            static bool isLowerCase(wchar_t);

            static bool isLowerCase(int);

            static bool isMirrored(wchar_t);

            static bool isMirrored(int);

            static bool isSpace(wchar_t);

            static bool isSpaceChar(wchar_t);

            static bool isSpaceChar(int);

            static bool isSupplementaryCodePoint(int);

            static bool isSurrogate(wchar_t);

            static bool isSurrogatePair(wchar_t, wchar_t);

            static bool isTitleCase(wchar_t);

            static bool isTitleCase(int);

            static bool isUnicodeIdentifierPart(wchar_t);

            static bool isUnicodeIdentifierPart(int);

            static bool isUnicodeIdentifierStart(wchar_t);

            static bool isUnicodeIdentifierStart(int);

            static bool isUpperCase(wchar_t);

            static bool isUpperCase(int);

            static bool isValidCodePoint(int);

            static bool isWhitespace(wchar_t);

            static bool isWhitespace(int);

            static wchar_t lowSurrogate(int);

            //static int offsetByCodePoints(std::vector<wchar_t> *, int, int, int, int);

            //static int offsetByCodePointsImpl(std::vector<wchar_t> *, int, int, int, int);

            static wchar_t reverseBytes(wchar_t);

            //static std::vector<wchar_t> *toChars(int);

            //static int toChars(int, std::vector<wchar_t> *, int);

            static int toCodePoint(wchar_t, wchar_t);

            static wchar_t toLowerCase(wchar_t);

            static int toLowerCase(int);

            //std::string *toString();

            //static std::string *toString(wchar_t);

            //static void toSurrogates(int, std::vector<wchar_t> *, int);

            static wchar_t toTitleCase(wchar_t);

            static int toTitleCase(int);

            static wchar_t toUpperCase(wchar_t);

            static int toUpperCase(int);

            //static std::vector<wchar_t> *toUpperCaseCharArray(int);

            static int toUpperCaseEx(int);

            static Character *valueOf(wchar_t);

        }; //class Character

    } //namespace java
} //namespace lang
