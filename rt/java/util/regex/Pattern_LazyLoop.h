#pragma once


namespace java{
namespace util{
namespace regex{

class LazyLoop: public Loop{
//methods
public:
    LazyLoop(int , int );

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool matchInit(Matcher* , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class LazyLoop

}//namespace java
}//namespace util
}//namespace regex
