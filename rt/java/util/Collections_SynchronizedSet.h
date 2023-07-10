#pragma once


namespace java{
namespace util{


template <typename E>
class SynchronizedSet: public SynchronizedCollection<E>{
//fields
public:
    static long serialVersionUID;

//methods
public:
    SynchronizedSet(std::unordered_set<E>* );

    SynchronizedSet(std::unordered_set<E>* , java::lang::Object* );

    bool equals(java::lang::Object* );

    int hashCode();


};//class SynchronizedSet

}//namespace java
}//namespace util
