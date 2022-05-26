#pragma once


namespace java{
namespace util{


template <typename E>
class UnmodifiableSortedSet: public UnmodifiableSet<E>, public SortedSet<E>, public java::io::Serializable{
//fields
public:
    static long serialVersionUID;
    SortedSet<E>* ss;

//methods
public:
    UnmodifiableSortedSet(SortedSet<E>* );

    Comparator<java::lang::Object*>* comparator();

    E first();

    SortedSet<E>* headSet(E );

    E last();

    SortedSet<E>* subSet(E , E );

    SortedSet<E>* tailSet(E );


};//class UnmodifiableSortedSet

}//namespace java
}//namespace util
