#pragma once


namespace java{
namespace util{
namespace regex{

class SliceI: public SliceNode{
//methods
public:
    SliceI(std::vector<int>* );

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class SliceI

}//namespace java
}//namespace util
}//namespace regex
