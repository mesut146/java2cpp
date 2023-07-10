#pragma once


namespace java{
namespace util{


template <typename E>
class CheckedQueue: public CheckedCollection<E>, public Queue<E>, public java::io::Serializable{
//fields
public:
    Queue<E>* queue;
    static long serialVersionUID;

//methods
public:
    CheckedQueue(Queue<E>* , java::lang::Class<E>* );

    E element();

    bool equals(java::lang::Object* );

    int hashCode();

    bool offer(E );

    E peek();

    E poll();

    E remove();


};//class CheckedQueue

}//namespace java
}//namespace util
