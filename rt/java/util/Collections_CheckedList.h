#pragma once


namespace java{
namespace util{


template <typename E>
class CheckedList: public CheckedCollection<E>{
//fields
public:
    std::vector<E>* list;
    static long serialVersionUID;

//methods
public:
    CheckedList(std::vector<E>* , java::lang::Class<E>* );

    void add(int , E );

    bool addAll(int , Collection<java::lang::Object*>* );

    bool equals(java::lang::Object* );

    E get(int );

    int hashCode();

    int indexOf(java::lang::Object* );

    int lastIndexOf(java::lang::Object* );

    ListIterator<E>* listIterator();

    ListIterator<E>* listIterator(int );

    E remove(int );

    void replaceAll(java::util::function::UnaryOperator<E>* );

    E set(int , E );

    void sort(Comparator<java::lang::Object*>* );

    virtual std::vector<E>* subList(int , int );


};//class CheckedList

}//namespace java
}//namespace util
