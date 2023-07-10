#pragma once


namespace java{
namespace util{
namespace regex{

class LastMatch: public Node{
//methods
public:
    LastMatch();

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class LastMatch

}//namespace java
}//namespace util
}//namespace regex
