#pragma once


namespace java{
namespace util{
namespace regex{

class BmpCharProperty: public CharProperty{
//methods
public:
    BmpCharProperty();

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class BmpCharProperty

}//namespace java
}//namespace util
}//namespace regex
