#pragma once


namespace java{
namespace util{
namespace regex{

class BnMS: public BnM{
//fields
public:
    int lengthInChars;

//methods
public:
    BnMS(std::vector<int>* , std::vector<int>* , std::vector<int>* , Node* );

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class BnMS

}//namespace java
}//namespace util
}//namespace regex
