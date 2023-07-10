#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class KeySpliterator: public TreeMapSpliterator<K, V>, public Spliterator<K>{
//methods
public:
    KeySpliterator(TreeMap<K, V>* , Entry<K, V>* , Entry<K, V>* , int , int , int );

    int characteristics();

    void forEachRemaining(java::util::function::Consumer<java::lang::Object*>* );

    Comparator<java::lang::Object*>* getComparator();

    bool tryAdvance(java::util::function::Consumer<java::lang::Object*>* );

    KeySpliterator<K, V>* trySplit();


};//class KeySpliterator

}//namespace java
}//namespace util
