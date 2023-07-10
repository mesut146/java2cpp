#pragma once


namespace java{
namespace util{
namespace regex{

class SliceIS: public SliceNode{
//methods
public:
    SliceIS(std::vector<int>* );

    bool match(Matcher* , int , java::lang::CharSequence* );

    virtual int toLower(int );


};//class SliceIS

}//namespace java
}//namespace util
}//namespace regex
