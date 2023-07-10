#pragma once


namespace java{
namespace util{
namespace regex{

class Utype: public CharProperty{
//fields
public:
    UnicodeProp* uprop;

//methods
public:
    Utype(UnicodeProp* );

    bool isSatisfiedBy(int );


};//class Utype

}//namespace java
}//namespace util
}//namespace regex
