#pragma once


namespace java{
namespace util{
namespace regex{

class Branch: public Node{
//fields
public:
    std::vector<Node*>* atoms;
    Node* conn;
    int size;

//methods
public:
    Branch(Node* , Node* , Node* );

    void add(Node* );

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class Branch

}//namespace java
}//namespace util
}//namespace regex
