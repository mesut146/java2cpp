#pragma once


namespace java{
namespace util{
namespace regex{

class GroupTail: public Node{
//fields
public:
    int groupIndex;
    int localIndex;

//methods
public:
    GroupTail(int , int );

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class GroupTail

}//namespace java
}//namespace util
}//namespace regex
