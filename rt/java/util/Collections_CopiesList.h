#pragma once


namespace java{
namespace util{


template <typename E>
class CopiesList: public AbstractList<E>, public RandomAccess, public java::io::Serializable{
//fields
public:
    static bool $assertionsDisabled;
    E element;
    int n;
    static long serialVersionUID;

//methods
public:
    CopiesList(int , E );

    bool contains(java::lang::Object* );

    bool equals(java::lang::Object* );

    E get(int );

    int hashCode();

    int indexOf(java::lang::Object* );

    int lastIndexOf(java::lang::Object* );

    java::util::stream::Stream<E>* parallelStream();

    void readObject(java::io::ObjectInputStream* );

    int size();

    Spliterator<E>* spliterator();

    java::util::stream::Stream<E>* stream();

    std::vector<E>* subList(int , int );

    std::vector<java::lang::Object*>* toArray();

    template <typename T>
    std::vector<T>* toArray(std::vector<T>* );


};//class CopiesList

}//namespace java
}//namespace util
