#pragma once


namespace java{
namespace util{
namespace regex{

class Node
{
//fields
public:
    Node* next;

//methods
public:
    Node();

    virtual bool match(Matcher* , int , java::lang::CharSequence* );

    virtual bool study(TreeInfo* );


};//class Node

}//namespace java
}//namespace util
}//namespace regex
