#pragma once


namespace java{
namespace util{
namespace regex{

class SliceU: public SliceNode{
//methods
public:
    SliceU(std::vector<int>* );

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class SliceU

}//namespace java
}//namespace util
}//namespace regex
