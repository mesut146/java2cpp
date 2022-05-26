#pragma once


namespace java{
namespace util{

class DateTime
{
//fields
public:
    static wchar_t AM_PM;
    static wchar_t CENTURY;
    static wchar_t DATE;
    static wchar_t DATE_TIME;
    static wchar_t DAY_OF_MONTH;
    static wchar_t DAY_OF_MONTH_0;
    static wchar_t DAY_OF_YEAR;
    static wchar_t HOUR;
    static wchar_t HOUR_0;
    static wchar_t HOUR_OF_DAY;
    static wchar_t HOUR_OF_DAY_0;
    static wchar_t ISO_STANDARD_DATE;
    static wchar_t MILLISECOND;
    static wchar_t MILLISECOND_SINCE_EPOCH;
    static wchar_t MINUTE;
    static wchar_t MONTH;
    static wchar_t NAME_OF_DAY;
    static wchar_t NAME_OF_DAY_ABBREV;
    static wchar_t NAME_OF_MONTH;
    static wchar_t NAME_OF_MONTH_ABBREV;
    static wchar_t NAME_OF_MONTH_ABBREV_X;
    static wchar_t NANOSECOND;
    static wchar_t SECOND;
    static wchar_t SECONDS_SINCE_EPOCH;
    static wchar_t TIME;
    static wchar_t TIME_12_HOUR;
    static wchar_t TIME_24_HOUR;
    static wchar_t YEAR_2;
    static wchar_t YEAR_4;
    static wchar_t ZONE;
    static wchar_t ZONE_NUMERIC;

//methods
public:
    DateTime();

    static bool isValid(wchar_t );


};//class DateTime

}//namespace java
}//namespace util
