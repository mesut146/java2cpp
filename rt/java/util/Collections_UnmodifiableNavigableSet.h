#pragma once


namespace java{
namespace util{


template <typename E>
class UnmodifiableNavigableSet: public UnmodifiableSortedSet<E>, public NavigableSet<E>, public java::io::Serializable{
//fields
public:
    static NavigableSet<java::lang::Object*>* EMPTY_NAVIGABLE_SET;
    NavigableSet<E>* ns;
    static long serialVersionUID;

//methods
public:
    UnmodifiableNavigableSet(NavigableSet<E>* );

    E ceiling(E );

    Iterator<E>* descendingIterator();

    NavigableSet<E>* descendingSet();

    E floor(E );

    NavigableSet<E>* headSet(E , bool );

    E higher(E );

    E lower(E );

    E pollFirst();

    E pollLast();

    NavigableSet<E>* subSet(E , bool , E , bool );

    NavigableSet<E>* tailSet(E , bool );


};//class UnmodifiableNavigableSet

}//namespace java
}//namespace util
