#pragma once


namespace java{
namespace util{
namespace regex{

class SliceNode: public Node{
//fields
public:
    std::vector<int>* buffer;

//methods
public:
    SliceNode(std::vector<int>* );

    bool study(TreeInfo* );


};//class SliceNode

}//namespace java
}//namespace util
}//namespace regex
