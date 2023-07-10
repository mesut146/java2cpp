#pragma once


namespace java{
namespace util{


template <typename E>
class CheckedNavigableSet: public CheckedSortedSet<E>, public NavigableSet<E>, public java::io::Serializable{
//fields
public:
    NavigableSet<E>* ns;
    static long serialVersionUID;

//methods
public:
    CheckedNavigableSet(NavigableSet<E>* , java::lang::Class<E>* );

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


};//class CheckedNavigableSet

}//namespace java
}//namespace util
