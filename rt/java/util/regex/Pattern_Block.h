#pragma once


namespace java{
namespace util{
namespace regex{

class Block: public CharProperty{
//fields
public:
    java::lang::UnicodeBlock* block;

//methods
public:
    Block(java::lang::UnicodeBlock* );

    bool isSatisfiedBy(int );


};//class Block

}//namespace java
}//namespace util
}//namespace regex
