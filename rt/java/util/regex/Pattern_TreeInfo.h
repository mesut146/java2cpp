#pragma once


namespace java{
namespace util{
namespace regex{

class TreeInfo
{
//fields
public:
    bool deterministic;
    int maxLength;
    bool maxValid;
    int minLength;

//methods
public:
    TreeInfo();

    void reset();


};//class TreeInfo

}//namespace java
}//namespace util
}//namespace regex
