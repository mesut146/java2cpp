#pragma once


namespace java{
namespace util{
namespace regex{

class BackRef: public Node{
//fields
public:
    int groupIndex;

//methods
public:
    BackRef(int );

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class BackRef

}//namespace java
}//namespace util
}//namespace regex
