#pragma once


namespace java{
namespace util{
namespace regex{

class Begin: public Node{
//methods
public:
    Begin();

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class Begin

}//namespace java
}//namespace util
}//namespace regex
