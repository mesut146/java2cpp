#pragma once


namespace java{
namespace util{
namespace regex{

class GroupRef: public Node{
//fields
public:
    GroupHead* head;

//methods
public:
    GroupRef(GroupHead* );

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class GroupRef

}//namespace java
}//namespace util
}//namespace regex
