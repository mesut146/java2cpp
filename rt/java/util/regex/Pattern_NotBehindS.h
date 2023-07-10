#pragma once


namespace java{
namespace util{
namespace regex{

class NotBehindS: public NotBehind{
//methods
public:
    NotBehindS(Node* , int , int );

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class NotBehindS

}//namespace java
}//namespace util
}//namespace regex
