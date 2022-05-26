#pragma once


namespace java{
namespace util{
namespace regex{

class End: public Node{
//methods
public:
    End();

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class End

}//namespace java
}//namespace util
}//namespace regex
