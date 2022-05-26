#pragma once


namespace java{
namespace util{
namespace regex{

class Prolog: public Node{
//fields
public:
    Loop* loop;

//methods
public:
    Prolog(Loop* );

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class Prolog

}//namespace java
}//namespace util
}//namespace regex
