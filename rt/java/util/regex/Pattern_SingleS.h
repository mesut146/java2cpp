#pragma once


namespace java{
namespace util{
namespace regex{

class SingleS: public CharProperty{
//fields
public:
    int c;

//methods
public:
    SingleS(int );

    bool isSatisfiedBy(int );


};//class SingleS

}//namespace java
}//namespace util
}//namespace regex
