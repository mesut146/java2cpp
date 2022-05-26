#pragma once


namespace java{
namespace util{
namespace regex{

class Loop: public Node{
//fields
public:
    int beginIndex;
    Node* body;
    int cmax;
    int cmin;
    int countIndex;

//methods
public:
    Loop(int , int );

    virtual bool match(Matcher* , int , java::lang::CharSequence* );

    virtual bool matchInit(Matcher* , int , java::lang::CharSequence* );

    virtual bool study(TreeInfo* );


};//class Loop

}//namespace java
}//namespace util
}//namespace regex
