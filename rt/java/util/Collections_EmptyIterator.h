#pragma once


namespace java{
namespace util{


template <typename E>
class EmptyIterator: public Iterator<E>{
//fields
public:
    static EmptyIterator<java::lang::Object*>* EMPTY_ITERATOR;

//methods
public:
    EmptyIterator();

    void forEachRemaining(java::util::function::Consumer<java::lang::Object*>* );

    bool hasNext();

    E next();

    void remove();


};//class EmptyIterator

}//namespace java
}//namespace util
