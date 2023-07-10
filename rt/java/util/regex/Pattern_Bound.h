#pragma once


namespace java{
namespace util{
namespace regex{

class Bound: public Node{
//fields
public:
    static int BOTH;
    static int LEFT;
    static int NONE;
    static int RIGHT;
    int type;
    bool useUWORD;

//methods
public:
    Bound(int , bool );

    int check(Matcher* , int , java::lang::CharSequence* );

    bool isWord(int );

    bool match(Matcher* , int , java::lang::CharSequence* );


};//class Bound

}//namespace java
}//namespace util
}//namespace regex
