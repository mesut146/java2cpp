#pragma once


namespace java{
namespace util{
namespace regex{

class UnixCaret: public Node{
//methods
public:
    UnixCaret();

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class UnixCaret

}//namespace java
}//namespace util
}//namespace regex
