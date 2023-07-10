#pragma once


namespace java{
namespace util{
namespace regex{

class NotBehind: public Node{
//fields
public:
    Node* cond;
    int rmax;
    int rmin;

//methods
public:
    NotBehind(Node* , int , int );

    virtual bool match(Matcher* , int , java::lang::CharSequence* );


};//class NotBehind

}//namespace java
}//namespace util
}//namespace regex
