#pragma once


namespace java{
namespace util{
namespace regex{

class Dollar: public Node{
//fields
public:
    bool multiline;

//methods
public:
    Dollar(bool );

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class Dollar

}//namespace java
}//namespace util
}//namespace regex
