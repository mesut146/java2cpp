#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class KeySpliterator: public HashMapSpliterator<K, V>, public Spliterator<K>{
//methods
public:
    KeySpliterator(HashMap<K, V>* , int , int , int , int );

    int characteristics();

    void forEachRemaining(java::util::function::Consumer<java::lang::Object*>* );

    bool tryAdvance(java::util::function::Consumer<java::lang::Object*>* );

    KeySpliterator<K, V>* trySplit();


};//class KeySpliterator

}//namespace java
}//namespace util
