#pragma once


namespace java{
namespace util{


template <typename T>
class SubMapIterator: public Iterator<T>{
//fields
public:
    int expectedModCount;
    java::lang::Object* fenceKey;
    Entry<K, V>* lastReturned;
    Entry<K, V>* next;
    NavigableSubMap* this$0;

//methods
public:
    SubMapIterator(Entry<K, V>* , Entry<K, V>* );

    bool hasNext();

    Entry<K, V>* nextEntry();

    Entry<K, V>* prevEntry();

    void removeAscending();

    void removeDescending();


};//class SubMapIterator

}//namespace java
}//namespace util
