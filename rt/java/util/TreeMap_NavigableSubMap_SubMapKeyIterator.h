#pragma once


namespace java{
namespace util{

class SubMapKeyIterator: public SubMapIterator<K>, public Spliterator<K>{
//fields
public:
    NavigableSubMap<K, V>* _parent_ref;
    NavigableSubMap* this$0;

//methods
public:
    SubMapKeyIterator* setRef(NavigableSubMap<K, V>* );

    SubMapKeyIterator(Entry<K, V>* , Entry<K, V>* );

    int characteristics();

    long estimateSize();

    void forEachRemaining(java::util::function::Consumer<java::lang::Object*>* );

    Comparator<java::lang::Object*>* getComparator();

    K next();

    void remove();

    bool tryAdvance(java::util::function::Consumer<java::lang::Object*>* );

    Spliterator<K>* trySplit();


};//class SubMapKeyIterator

}//namespace java
}//namespace util
