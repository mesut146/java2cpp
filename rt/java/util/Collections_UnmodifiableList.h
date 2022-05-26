#pragma once


namespace java{
namespace util{


template <typename E>
class UnmodifiableList: public UnmodifiableCollection<E>{
//fields
public:
    std::vector<java::lang::Object*>* list;
    static long serialVersionUID;

//methods
public:
    UnmodifiableList(std::vector<java::lang::Object*>* );

    void add(int , E );

    bool addAll(int , Collection<java::lang::Object*>* );

    bool equals(java::lang::Object* );

    E get(int );

    int hashCode();

    int indexOf(java::lang::Object* );

    int lastIndexOf(java::lang::Object* );

    ListIterator<E>* listIterator();

    ListIterator<E>* listIterator(int );

    java::lang::Object* readResolve();

    E remove(int );

    void replaceAll(java::util::function::UnaryOperator<E>* );

    E set(int , E );

    void sort(Comparator<java::lang::Object*>* );

    virtual std::vector<E>* subList(int , int );


};//class UnmodifiableList

}//namespace java
}//namespace util
