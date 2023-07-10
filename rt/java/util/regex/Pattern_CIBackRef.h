#pragma once


namespace java{
namespace util{
namespace regex{

class CIBackRef: public Node{
//fields
public:
    bool doUnicodeCase;
    int groupIndex;

//methods
public:
    CIBackRef(int , bool );

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class CIBackRef

}//namespace java
}//namespace util
}//namespace regex
