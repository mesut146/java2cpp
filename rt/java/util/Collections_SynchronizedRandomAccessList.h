#pragma once


namespace java{
namespace util{


template <typename E>
class SynchronizedRandomAccessList: public SynchronizedList<E>, public RandomAccess{
//fields
public:
    static long serialVersionUID;

//methods
public:
    SynchronizedRandomAccessList(std::vector<E>* );

    SynchronizedRandomAccessList(std::vector<E>* , java::lang::Object* );

    std::vector<E>* subList(int , int );

    java::lang::Object* writeReplace();


};//class SynchronizedRandomAccessList

}//namespace java
}//namespace util
