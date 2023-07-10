#pragma once


namespace java{
namespace util{
namespace regex{

class BehindS: public Behind{
//methods
public:
    BehindS(Node* , int , int );

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class BehindS

}//namespace java
}//namespace util
}//namespace regex
