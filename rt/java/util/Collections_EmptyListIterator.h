#pragma once


namespace java{
namespace util{


template <typename E>
class EmptyListIterator: public EmptyIterator<E>, public ListIterator<E>{
//fields
public:
    static EmptyListIterator<java::lang::Object*>* EMPTY_ITERATOR;

//methods
public:
    EmptyListIterator();

    void add(E );

    bool hasPrevious();

    int nextIndex();

    E previous();

    int previousIndex();

    void set(E );


};//class EmptyListIterator

}//namespace java
}//namespace util
