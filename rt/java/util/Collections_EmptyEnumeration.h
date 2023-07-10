#pragma once


namespace java{
namespace util{


template <typename E>
class EmptyEnumeration: public Enumeration<E>{
//fields
public:
    static EmptyEnumeration<java::lang::Object*>* EMPTY_ENUMERATION;

//methods
public:
    EmptyEnumeration();

    bool hasMoreElements();

    E nextElement();


};//class EmptyEnumeration

}//namespace java
}//namespace util
