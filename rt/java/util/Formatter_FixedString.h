#pragma once


namespace java{
namespace util{

class FixedString: public FormatString{
//fields
public:
    Formatter* _parent_ref;
    std::string* s;
    Formatter* this$0;

//methods
public:
    FixedString* setRef(Formatter* );

    FixedString(std::string* );

    int index();

    void print(java::lang::Object* , Locale* );

    std::string* toString();


};//class FixedString

}//namespace java
}//namespace util
