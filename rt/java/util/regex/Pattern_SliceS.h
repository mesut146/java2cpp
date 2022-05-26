#pragma once


namespace java{
namespace util{
namespace regex{

class SliceS: public SliceNode{
//methods
public:
    SliceS(std::vector<int>* );

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class SliceS

}//namespace java
}//namespace util
}//namespace regex
