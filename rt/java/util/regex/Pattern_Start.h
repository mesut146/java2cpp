#pragma once


namespace java{
namespace util{
namespace regex{

class Start: public Node{
//fields
public:
    int minLength;

//methods
public:
    Start(Node* );

    virtual bool match(Matcher* , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class Start

}//namespace java
}//namespace util
}//namespace regex
