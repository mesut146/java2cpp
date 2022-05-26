#pragma once


namespace java{
namespace util{
namespace regex{

class GroupHead: public Node{
//fields
public:
    int localIndex;

//methods
public:
    GroupHead(int );

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool matchRef(Matcher* , int , java::lang::CharSequence* );


};//class GroupHead

}//namespace java
}//namespace util
}//namespace regex
