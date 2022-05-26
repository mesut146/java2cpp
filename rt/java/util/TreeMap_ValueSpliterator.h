#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class ValueSpliterator: public TreeMapSpliterator<K, V>, public Spliterator<V>{
//methods
public:
    ValueSpliterator(TreeMap<K, V>* , Entry<K, V>* , Entry<K, V>* , int , int , int );

    int characteristics();

    void forEachRemaining(java::util::function::Consumer<java::lang::Object*>* );

    bool tryAdvance(java::util::function::Consumer<java::lang::Object*>* );

    ValueSpliterator<K, V>* trySplit();


};//class ValueSpliterator

}//namespace java
}//namespace util
