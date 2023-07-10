#pragma once


namespace java{
namespace util{
namespace regex{

class LineEnding: public Node{
//methods
public:
    LineEnding();

    bool match(Matcher* , int , java::lang::CharSequence* );

    bool study(TreeInfo* );


};//class LineEnding

}//namespace java
}//namespace util
}//namespace regex
