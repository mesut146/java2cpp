#pragma once


namespace java{
namespace util{

class BigDecimalLayout
{
//fields
public:
    FormatSpecifier* _parent_ref;
    bool dot;
    java::lang::StringBuilder* exp;
    java::lang::StringBuilder* mant;
    int scale_renamed;
    FormatSpecifier* this$1;

//methods
public:
    BigDecimalLayout* setRef(FormatSpecifier* );

    BigDecimalLayout(java::math::BigInteger* , int , BigDecimalLayoutForm* );

    std::vector<wchar_t>* exponent();

    bool hasDot();

    void layout(java::math::BigInteger* , int , BigDecimalLayoutForm* );

    std::vector<wchar_t>* layoutChars();

    std::vector<wchar_t>* mantissa();

    int scale();

    std::vector<wchar_t>* toCharArray(java::lang::StringBuilder* );


};//class BigDecimalLayout

}//namespace java
}//namespace util
