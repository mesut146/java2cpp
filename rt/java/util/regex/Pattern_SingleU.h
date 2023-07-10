#pragma once


namespace java{
namespace util{
namespace regex{

class SingleU: public CharProperty{
//fields
public:
    int lower;

//methods
public:
    SingleU(int );

    bool isSatisfiedBy(int );


};//class SingleU

}//namespace java
}//namespace util
}//namespace regex
