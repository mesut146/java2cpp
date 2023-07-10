#pragma once


namespace java{
namespace util{
namespace regex{

class BnM: public Node{
//fields
public:
    std::vector<int>* buffer;
    std::vector<int>* lastOcc;
    std::vector<int>* optoSft;

//methods
public:
    BnM(std::vector<int>* , std::vector<int>* , std::vector<int>* , Node* );

    virtual bool match(Matcher* , int , java::lang::CharSequence* );

    static Node* optimize(Node* );

    bool study(TreeInfo* );


};//class BnM

}//namespace java
}//namespace util
}//namespace regex
