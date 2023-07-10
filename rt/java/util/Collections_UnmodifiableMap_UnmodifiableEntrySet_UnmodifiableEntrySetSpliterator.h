#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class UnmodifiableEntrySetSpliterator: public Spliterator<Entry<K, V>*>{
//fields
public:
    Spliterator<Entry<K, V>*>* s;

//methods
public:
    UnmodifiableEntrySetSpliterator(Spliterator<Entry<K, V>*>* );

    int characteristics();

    long estimateSize();

    void forEachRemaining(java::util::function::Consumer<java::lang::Object*>* );

    Comparator<java::lang::Object*>* getComparator();

    long getExactSizeIfKnown();

    bool hasCharacteristics(int );

    bool tryAdvance(java::util::function::Consumer<java::lang::Object*>* );

    Spliterator<Entry<K, V>*>* trySplit();


};//class UnmodifiableEntrySetSpliterator

}//namespace java
}//namespace util
