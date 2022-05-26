#pragma once


namespace java{
namespace util{


template <typename E>
class CheckedSortedSet: public CheckedSet<E>, public SortedSet<E>, public java::io::Serializable{
//fields
public:
    static long serialVersionUID;
    SortedSet<E>* ss;

//methods
public:
    virtual SortedSet<E>* headSet(E );

    virtual SortedSet<E>* subSet(E , E );

    virtual SortedSet<E>* tailSet(E );

    CheckedSortedSet(SortedSet<E>* , java::lang::Class<E>* );

    Comparator<java::lang::Object*>* comparator();

    E first();

    E last();


};//class CheckedSortedSet

}//namespace java
}//namespace util
