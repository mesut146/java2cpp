#pragma once


namespace java{
namespace util{


template <typename E>
class SingletonSet: public AbstractSet<E>, public java::io::Serializable{
//fields
public:
    E element;
    static long serialVersionUID;

//methods
public:
    SingletonSet(E );

    bool contains(java::lang::Object* );

    void forEach(java::util::function::Consumer<java::lang::Object*>* );

    Iterator<E>* iterator();

    bool removeIf(java::util::function::Predicate<java::lang::Object*>* );

    int size();

    Spliterator<E>* spliterator();


};//class SingletonSet

}//namespace java
}//namespace util
