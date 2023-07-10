#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class HashMapSpliterator
{
//fields
public:
    Node<K, V>* current;
    int est;
    int expectedModCount;
    int fence;
    int index;
    HashMap<K, V>* map;

//methods
public:
    HashMapSpliterator(HashMap<K, V>* , int , int , int , int );

    long estimateSize();

    int getFence();


};//class HashMapSpliterator

}//namespace java
}//namespace util
