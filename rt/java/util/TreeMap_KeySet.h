#pragma once


namespace java{
namespace util{


template <typename E>
class KeySet: public AbstractSet<E>, public NavigableSet<E>{
//fields
public:
    NavigableMap<E, java::lang::Object*>* m;

//methods
public:
    KeySet(NavigableMap<E, java::lang::Object*>* );

    E ceiling(E );

    void clear();

    Comparator<java::lang::Object*>* comparator();

    bool contains(java::lang::Object* );

    Iterator<E>* descendingIterator();

    NavigableSet<E>* descendingSet();

    E first();

    E floor(E );

    SortedSet<E>* headSet(E );

    NavigableSet<E>* headSet(E , bool );

    E higher(E );

    bool isEmpty();

    Iterator<E>* iterator();

    E last();

    E lower(E );

    E pollFirst();

    E pollLast();

    bool remove(java::lang::Object* );

    int size();

    Spliterator<E>* spliterator();

    SortedSet<E>* subSet(E , E );

    NavigableSet<E>* subSet(E , bool , E , bool );

    SortedSet<E>* tailSet(E );

    NavigableSet<E>* tailSet(E , bool );


};//class KeySet

}//namespace java
}//namespace util
