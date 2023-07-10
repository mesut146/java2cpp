#pragma once


namespace java{
namespace util{
namespace regex{

class First: public Node{
//fields
public:
    Node* atom;

//methods
public:
    First(Node* );

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class First

}//namespace java
}//namespace util
}//namespace regex
