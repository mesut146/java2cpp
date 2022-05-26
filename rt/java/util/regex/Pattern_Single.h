#pragma once


namespace java{
namespace util{
namespace regex{

class Single: public BmpCharProperty{
//fields
public:
    int c;

//methods
public:
    Single(int );

    bool isSatisfiedBy(int );


};//class Single

}//namespace java
}//namespace util
}//namespace regex
