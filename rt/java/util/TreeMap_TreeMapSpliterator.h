#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class TreeMapSpliterator
{
//fields
public:
    Entry<K, V>* current;
    int est;
    int expectedModCount;
    Entry<K, V>* fence;
    int side;
    TreeMap<K, V>* tree;

//methods
public:
    TreeMapSpliterator(TreeMap<K, V>* , Entry<K, V>* , Entry<K, V>* , int , int , int );

    long estimateSize();

    int getEstimate();


};//class TreeMapSpliterator

}//namespace java
}//namespace util
