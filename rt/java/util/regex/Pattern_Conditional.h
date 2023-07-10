#pragma once


namespace java{
namespace util{
namespace regex{

class Conditional: public Node{
//fields
public:
    Node* cond;
    Node* not_renamed;
    Node* yes;

//methods
public:
    Conditional(Node* , Node* , Node* );

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class Conditional

}//namespace java
}//namespace util
}//namespace regex
