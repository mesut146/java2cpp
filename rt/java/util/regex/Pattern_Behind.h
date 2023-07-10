#pragma once


namespace java{
namespace util{
namespace regex{

class Behind: public Node{
//fields
public:
    Node* cond;
    int rmax;
    int rmin;

//methods
public:
    Behind(Node* , int , int );

    virtual bool match(Matcher* , int , java::lang::CharSequence* );


};//class Behind

}//namespace java
}//namespace util
}//namespace regex
