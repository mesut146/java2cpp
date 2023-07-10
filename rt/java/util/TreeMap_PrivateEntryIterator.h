#pragma once


namespace java{
namespace util{


template <typename T>
class PrivateEntryIterator: public Iterator<T>{
//fields
public:
    int expectedModCount;
    Entry<K, V>* lastReturned;
    Entry<K, V>* next;
    TreeMap* this$0;

//methods
public:
    PrivateEntryIterator(Entry<K, V>* );

    virtual void remove();

    bool hasNext();

    Entry<K, V>* nextEntry();

    Entry<K, V>* prevEntry();


};//class PrivateEntryIterator

}//namespace java
}//namespace util
