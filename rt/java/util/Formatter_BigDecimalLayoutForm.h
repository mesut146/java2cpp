#pragma once


namespace java{
namespace util{

class BigDecimalLayoutForm: public java::lang::Enum<BigDecimalLayoutForm*>{
//fields
public:
    static std::vector<BigDecimalLayoutForm*>* $VALUES;
    static BigDecimalLayoutForm* DECIMAL_FLOAT;
    static BigDecimalLayoutForm* SCIENTIFIC;

//methods
public:
    BigDecimalLayoutForm();

    static BigDecimalLayoutForm* valueOf(std::string* );

    static std::vector<BigDecimalLayoutForm*>* values();


};//class BigDecimalLayoutForm

}//namespace java
}//namespace util
