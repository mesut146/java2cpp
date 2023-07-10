#pragma once


namespace java{
namespace util{
namespace regex{

class Neg: public Node{
//fields
public:
    Node* cond;

//methods
public:
    Neg(Node* );

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class Neg

}//namespace java
}//namespace util
}//namespace regex
