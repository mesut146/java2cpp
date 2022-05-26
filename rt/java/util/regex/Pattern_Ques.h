#pragma once


namespace java{
namespace util{
namespace regex{

class Ques: public Node{
//fields
public:
    Node* atom;
    int type;

//methods
public:
    Ques(Node* , int );

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class Ques

}//namespace java
}//namespace util
}//namespace regex
