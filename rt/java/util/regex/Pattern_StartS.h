#pragma once


namespace java{
namespace util{
namespace regex{

class StartS: public Start{
//methods
public:
    StartS(Node* );

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class StartS

}//namespace java
}//namespace util
}//namespace regex
