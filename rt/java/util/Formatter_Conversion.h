#pragma once


namespace java{
namespace util{

class Conversion
{
//fields
public:
    static wchar_t BOOLEAN;
    static wchar_t BOOLEAN_UPPER;
    static wchar_t CHARACTER;
    static wchar_t CHARACTER_UPPER;
    static wchar_t DATE_TIME;
    static wchar_t DATE_TIME_UPPER;
    static wchar_t DECIMAL_FLOAT;
    static wchar_t DECIMAL_INTEGER;
    static wchar_t GENERAL;
    static wchar_t GENERAL_UPPER;
    static wchar_t HASHCODE;
    static wchar_t HASHCODE_UPPER;
    static wchar_t HEXADECIMAL_FLOAT;
    static wchar_t HEXADECIMAL_FLOAT_UPPER;
    static wchar_t HEXADECIMAL_INTEGER;
    static wchar_t HEXADECIMAL_INTEGER_UPPER;
    static wchar_t LINE_SEPARATOR;
    static wchar_t OCTAL_INTEGER;
    static wchar_t PERCENT_SIGN;
    static wchar_t SCIENTIFIC;
    static wchar_t SCIENTIFIC_UPPER;
    static wchar_t STRING;
    static wchar_t STRING_UPPER;

//methods
public:
    Conversion();

    static bool isCharacter(wchar_t );

    static bool isFloat(wchar_t );

    static bool isGeneral(wchar_t );

    static bool isInteger(wchar_t );

    static bool isText(wchar_t );

    static bool isValid(wchar_t );


};//class Conversion

}//namespace java
}//namespace util
