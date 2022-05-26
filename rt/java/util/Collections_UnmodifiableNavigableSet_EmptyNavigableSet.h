#pragma once


namespace java{
namespace util{


template <typename E>
class EmptyNavigableSet: public UnmodifiableNavigableSet<E>, public java::io::Serializable{
//fields
public:
    static long serialVersionUID;

//methods
public:
    EmptyNavigableSet();

    java::lang::Object* readResolve();


};//class EmptyNavigableSet

}//namespace java
}//namespace util
