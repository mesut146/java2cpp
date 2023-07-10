#pragma once


namespace java{
namespace util{


template <typename E>
class SynchronizedNavigableSet: public SynchronizedSortedSet<E>, public NavigableSet<E>{
//fields
public:
    NavigableSet<E>* ns;
    static long serialVersionUID;

//methods
public:
    SynchronizedNavigableSet(NavigableSet<E>* );

    SynchronizedNavigableSet(NavigableSet<E>* , java::lang::Object* );

    E ceiling(E );

    Iterator<E>* descendingIterator();

    NavigableSet<E>* descendingSet();

    E floor(E );

    NavigableSet<E>* headSet(E );

    NavigableSet<E>* headSet(E , bool );

    E higher(E );

    E lower(E );

    E pollFirst();

    E pollLast();

    NavigableSet<E>* subSet(E , E );

    NavigableSet<E>* subSet(E , bool , E , bool );

    NavigableSet<E>* tailSet(E );

    NavigableSet<E>* tailSet(E , bool );


};//class SynchronizedNavigableSet

}//namespace java
}//namespace util
