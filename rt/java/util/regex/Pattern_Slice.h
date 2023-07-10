#pragma once


namespace java{
namespace util{
namespace regex{

class Slice: public SliceNode{
//methods
public:
    Slice(std::vector<int>* );

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class Slice

}//namespace java
}//namespace util
}//namespace regex
