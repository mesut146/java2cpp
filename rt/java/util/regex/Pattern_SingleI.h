#pragma once


namespace java{
namespace util{
namespace regex{

class SingleI: public BmpCharProperty{
//fields
public:
    int lower;
    int upper;

//methods
public:
    SingleI(int , int );

    bool isSatisfiedBy(int );


};//class SingleI

}//namespace java
}//namespace util
}//namespace regex
