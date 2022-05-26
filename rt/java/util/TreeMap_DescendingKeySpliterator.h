#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class DescendingKeySpliterator: public TreeMapSpliterator<K, V>, public Spliterator<K>{
//methods
public:
    DescendingKeySpliterator(TreeMap<K, V>* , Entry<K, V>* , Entry<K, V>* , int , int , int );

    int characteristics();

    void forEachRemaining(java::util::function::Consumer<java::lang::Object*>* );

    bool tryAdvance(java::util::function::Consumer<java::lang::Object*>* );

    DescendingKeySpliterator<K, V>* trySplit();


};//class DescendingKeySpliterator

}//namespace java
}//namespace util
