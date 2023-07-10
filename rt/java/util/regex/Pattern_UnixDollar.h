#pragma once


namespace java{
namespace util{
namespace regex{

class UnixDollar: public Node{
//fields
public:
    bool multiline;

//methods
public:
    UnixDollar(bool );

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class UnixDollar

}//namespace java
}//namespace util
}//namespace regex
