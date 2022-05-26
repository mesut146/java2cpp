#pragma once


namespace java{
namespace util{
namespace regex{

class Pos: public Node{
//fields
public:
    Node* cond;

//methods
public:
    Pos(Node* );

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class Pos

}//namespace java
}//namespace util
}//namespace regex
