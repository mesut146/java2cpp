#pragma once


namespace java{
namespace util{


template <typename E>
class SynchronizedSortedSet: public SynchronizedSet<E>, public SortedSet<E>{
//fields
public:
    static long serialVersionUID;
    SortedSet<E>* ss;

//methods
public:
    virtual SortedSet<E>* headSet(E );

    virtual SortedSet<E>* subSet(E , E );

    virtual SortedSet<E>* tailSet(E );

    SynchronizedSortedSet(SortedSet<E>* );

    SynchronizedSortedSet(SortedSet<E>* , java::lang::Object* );

    Comparator<java::lang::Object*>* comparator();

    E first();

    E last();


};//class SynchronizedSortedSet

}//namespace java
}//namespace util
