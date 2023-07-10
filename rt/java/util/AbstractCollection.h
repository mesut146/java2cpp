#pragma once


namespace java{
namespace util{


template <typename E>
class AbstractCollection: public Collection<E>{
//fields
public:
    static int MAX_ARRAY_SIZE;

//methods
public:
    AbstractCollection();

    virtual void clear();

    virtual bool contains(java::lang::Object* );

    virtual Iterator<E>* iterator() = 0;

    virtual int size() = 0;

    virtual bool remove(java::lang::Object* );

    virtual bool add(E );

    virtual bool removeAll(Collection<java::lang::Object*>* );

    bool addAll(Collection<java::lang::Object*>* );

    bool containsAll(Collection<java::lang::Object*>* );

    template <typename T>
    static std::vector<T>* finishToArray(std::vector<T>* , Iterator<java::lang::Object*>* );

    static int hugeCapacity(int );

    bool isEmpty();

    bool retainAll(Collection<java::lang::Object*>* );

    std::vector<java::lang::Object*>* toArray();

    template <typename T>
    std::vector<T>* toArray(std::vector<T>* );

    std::string* toString();


};//class AbstractCollection

}//namespace java
}//namespace util
