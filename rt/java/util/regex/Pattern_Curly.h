#pragma once


namespace java{
namespace util{
namespace regex{

class Curly: public Node{
//fields
public:
    Node* atom;
    int cmax;
    int cmin;
    int type;

//methods
public:
    Curly(Node* , int , int , int );

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool match0(Matcher* , int , int , java::lang::CharSequence* );

    bool match1(Matcher* , int , int , java::lang::CharSequence* );

    bool match2(Matcher* , int , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class Curly

}//namespace java
}//namespace util
}//namespace regex
