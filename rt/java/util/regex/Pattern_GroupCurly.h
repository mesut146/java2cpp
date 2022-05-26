#pragma once


namespace java{
namespace util{
namespace regex{

class GroupCurly: public Node{
//fields
public:
    Node* atom;
    bool capture;
    int cmax;
    int cmin;
    int groupIndex;
    int localIndex;
    int type;

//methods
public:
    GroupCurly(Node* , int , int , int , int , int , bool );

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool match0(Matcher* , int , int , java::lang::CharSequence* );

    bool match1(Matcher* , int , int , java::lang::CharSequence* );

    bool match2(Matcher* , int , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class GroupCurly

}//namespace java
}//namespace util
}//namespace regex
