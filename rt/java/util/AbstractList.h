#pragma once


namespace java{
namespace util{


template <typename E>
class AbstractList: public AbstractCollection<E>{
//methods
public:
    AbstractList();

    virtual E get(int ) = 0;

    virtual int indexOf(java::lang::Object* );

    virtual E set(int , E );

    virtual bool equals(java::lang::Object* );

    virtual int hashCode();

    virtual int lastIndexOf(java::lang::Object* );

    virtual std::vector<E>* subList(int , int );

    virtual Iterator<E>* iterator();

    virtual ListIterator<E>* listIterator();

    virtual bool add(E );

    virtual void add(int , E );

    virtual bool addAll(int , Collection<java::lang::Object*>* );

    virtual void clear();

    virtual ListIterator<E>* listIterator(int );

    virtual E remove(int );

    virtual void removeRange(int , int );


};//class AbstractList

}//namespace java
}//namespace util
