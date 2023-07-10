#pragma once


namespace java{
namespace util{


template <typename E>
class AbstractQueue: public AbstractCollection<E>, public Queue<E>{
//methods
public:
    virtual bool add(E );

    virtual void clear();

    virtual E element();

    virtual E remove();


};//class AbstractQueue

}//namespace java
}//namespace util
