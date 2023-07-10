#pragma once


namespace java{
namespace util{


template <typename E>
class CheckedRandomAccessList: public CheckedList<E>, public RandomAccess{
//fields
public:
    static long serialVersionUID;

//methods
public:
    CheckedRandomAccessList(std::vector<E>* , java::lang::Class<E>* );

    std::vector<E>* subList(int , int );


};//class CheckedRandomAccessList

}//namespace java
}//namespace util
