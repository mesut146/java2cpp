#pragma once


namespace java{
namespace util{


template <typename E>
class UnmodifiableRandomAccessList: public UnmodifiableList<E>, public RandomAccess{
//fields
public:
    static long serialVersionUID;

//methods
public:
    UnmodifiableRandomAccessList(std::vector<java::lang::Object*>* );

    std::vector<E>* subList(int , int );

    java::lang::Object* writeReplace();


};//class UnmodifiableRandomAccessList

}//namespace java
}//namespace util
