#pragma once


namespace java{
namespace util{
namespace regex{

class CharProperty: public Node{
//methods
public:
    CharProperty();

    virtual bool isSatisfiedBy(int ) = 0;

    virtual bool match(Matcher* , int , java::lang::CharSequence* );

    CharProperty* complement();

    bool study(TreeInfo* );


};//class CharProperty

}//namespace java
}//namespace util
}//namespace regex
