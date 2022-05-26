#pragma once


namespace java{
namespace util{


template <typename E>
class Stack: public Vector<E>{
//fields
public:
    static long serialVersionUID;

//methods
public:
    Stack();

    bool empty();

    E peek();

    E pop();

    E push(E );

    int search(java::lang::Object* );


};//class Stack

}//namespace java
}//namespace util
