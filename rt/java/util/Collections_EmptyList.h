#pragma once


namespace java{
namespace util{


template <typename E>
class EmptyList: public AbstractList<E>, public RandomAccess, public java::io::Serializable{
//fields
public:
    static long serialVersionUID;

//methods
public:
    EmptyList();

    bool contains(java::lang::Object* );

    bool containsAll(Collection<java::lang::Object*>* );

    bool equals(java::lang::Object* );

    void forEach(java::util::function::Consumer<java::lang::Object*>* );

    E get(int );

    int hashCode();

    bool isEmpty();

    Iterator<E>* iterator();

    ListIterator<E>* listIterator();

    java::lang::Object* readResolve();

    bool removeIf(java::util::function::Predicate<java::lang::Object*>* );

    void replaceAll(java::util::function::UnaryOperator<E>* );

    int size();

    void sort(Comparator<java::lang::Object*>* );

    Spliterator<E>* spliterator();

    std::vector<java::lang::Object*>* toArray();

    template <typename T>
    std::vector<T>* toArray(std::vector<T>* );


};//class EmptyList

}//namespace java
}//namespace util
