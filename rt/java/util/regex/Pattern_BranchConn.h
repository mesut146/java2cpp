#pragma once


namespace java{
namespace util{
namespace regex{

class BranchConn: public Node{
//methods
public:
    BranchConn();

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class BranchConn

}//namespace java
}//namespace util
}//namespace regex
