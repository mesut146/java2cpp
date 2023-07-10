#pragma once


namespace java{
namespace util{

class DescendingSubMapKeyIterator: public SubMapIterator<K>, public Spliterator<K>{
//fields
public:
    NavigableSubMap<K, V>* _parent_ref;
    NavigableSubMap* this$0;

//methods
public:
    DescendingSubMapKeyIterator* setRef(NavigableSubMap<K, V>* );

    DescendingSubMapKeyIterator(Entry<K, V>* , Entry<K, V>* );

    int characteristics();

    long estimateSize();

    void forEachRemaining(java::util::function::Consumer<java::lang::Object*>* );

    K next();

    void remove();

    bool tryAdvance(java::util::function::Consumer<java::lang::Object*>* );

    Spliterator<K>* trySplit();


};//class DescendingSubMapKeyIterator

}//namespace java
}//namespace util
