#pragma once


namespace java{
namespace util{
namespace regex{

class Caret: public Node{
//methods
public:
    Caret();

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class Caret

}//namespace java
}//namespace util
}//namespace regex
