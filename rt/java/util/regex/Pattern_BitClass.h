#pragma once


namespace java{
namespace util{
namespace regex{

class BitClass: public BmpCharProperty{
//fields
public:
    static bool $assertionsDisabled;
    std::vector<bool>* bits;

//methods
public:
    BitClass();

    BitClass(std::vector<bool>* );

    BitClass* add(int , int );

    bool isSatisfiedBy(int );


};//class BitClass

}//namespace java
}//namespace util
}//namespace regex
