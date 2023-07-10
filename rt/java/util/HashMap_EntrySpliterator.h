#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class EntrySpliterator: public HashMapSpliterator<K, V>, public Spliterator<Entry<K, V>*>{
//methods
public:
    EntrySpliterator(HashMap<K, V>* , int , int , int , int );

    int characteristics();

    void forEachRemaining(java::util::function::Consumer<java::lang::Object*>* );

    bool tryAdvance(java::util::function::Consumer<java::lang::Object*>* );

    EntrySpliterator<K, V>* trySplit();


};//class EntrySpliterator

}//namespace java
}//namespace util
