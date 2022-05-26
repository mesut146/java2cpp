#pragma once


namespace java{
namespace util{
namespace regex{

class Ctype: public BmpCharProperty{
//fields
public:
    int ctype;

//methods
public:
    Ctype(int );

    bool isSatisfiedBy(int );


};//class Ctype

}//namespace java
}//namespace util
}//namespace regex
