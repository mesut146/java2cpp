#pragma once


namespace java{
namespace util{


template <typename E>
class SingletonList: public AbstractList<E>, public RandomAccess, public java::io::Serializable{
//fields
public:
    E element;
    static long serialVersionUID;

//methods
public:
    SingletonList(E );

    bool contains(java::lang::Object* );

    void forEach(java::util::function::Consumer<java::lang::Object*>* );

    E get(int );

    Iterator<E>* iterator();

    bool removeIf(java::util::function::Predicate<java::lang::Object*>* );

    void replaceAll(java::util::function::UnaryOperator<E>* );

    int size();

    void sort(Comparator<java::lang::Object*>* );

    Spliterator<E>* spliterator();


};//class SingletonList

}//namespace java
}//namespace util
