#pragma once


namespace java{
namespace util{

class FormatSpecifier: public FormatString{
//fields
public:
    Formatter* _parent_ref;
    static bool $assertionsDisabled;
    wchar_t c;
    bool dt;
    Flags* f;
    int index_renamed;
    int precision_renamed;
    Formatter* this$0;
    int width_renamed;

//methods
public:
    FormatSpecifier* setRef(Formatter* );

    FormatSpecifier(java::util::regex::Matcher* );

    std::vector<wchar_t>* addDot(std::vector<wchar_t>* );

    std::vector<wchar_t>* addZeros(std::vector<wchar_t>* , int );

    int adjustWidth(int , Flags* , bool );

    void checkBadFlags(std::vector<Flags*>* );

    void checkCharacter();

    void checkDateTime();

    void checkFloat();

    void checkGeneral();

    void checkInteger();

    void checkNumeric();

    void checkText();

    wchar_t conversion();

    wchar_t conversion(std::string* );

    void failConversion(wchar_t , java::lang::Object* );

    void failMismatch(Flags* , wchar_t );

    Flags* flags();

    Flags* flags(std::string* );

    wchar_t getZero(Locale* );

    std::string* hexDouble(double , int );

    int index();

    int index(std::string* );

    std::string* justify(std::string* );

    java::lang::StringBuilder* leadingSign(java::lang::StringBuilder* , bool );

    java::lang::StringBuilder* localizedMagnitude(java::lang::StringBuilder* , long , Flags* , int , Locale* );

    java::lang::StringBuilder* localizedMagnitude(java::lang::StringBuilder* , std::vector<wchar_t>* , Flags* , int , Locale* );

    int precision();

    int precision(std::string* );

    void print(std::string* );

    void print(java::lang::Object* , Locale* );

    void print(char , Locale* );

    void print(char16_t , Locale* );

    void print(int , Locale* );

    void print(long , Locale* );

    void print(java::math::BigInteger* , Locale* );

    void print(float , Locale* );

    void print(double , Locale* );

    void print(java::math::BigDecimal* , Locale* );

    void print(Calendar* , wchar_t , Locale* );

    void print(java::time::temporal::TemporalAccessor* , wchar_t , Locale* );

    java::lang::Appendable* print(java::lang::StringBuilder* , Calendar* , wchar_t , Locale* );

    java::lang::Appendable* print(java::lang::StringBuilder* , java::time::temporal::TemporalAccessor* , wchar_t , Locale* );

    void print(java::lang::StringBuilder* , double , Locale* , Flags* , wchar_t , int , bool );

    void print(java::lang::StringBuilder* , java::math::BigDecimal* , Locale* , Flags* , wchar_t , int , bool );

    void printBoolean(java::lang::Object* );

    void printCharacter(java::lang::Object* );

    void printDateTime(java::lang::Object* , Locale* );

    void printFloat(java::lang::Object* , Locale* );

    void printHashCode(java::lang::Object* );

    void printInteger(java::lang::Object* , Locale* );

    void printString(java::lang::Object* , Locale* );

    std::string* toString();

    java::lang::StringBuilder* trailingSign(java::lang::StringBuilder* , bool );

    std::vector<wchar_t>* trailingZeros(std::vector<wchar_t>* , int );

    int width();

    int width(std::string* );


};//class FormatSpecifier

}//namespace java
}//namespace util
