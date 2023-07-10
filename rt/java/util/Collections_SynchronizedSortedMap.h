#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class SynchronizedSortedMap: public SynchronizedMap<K, V>, public SortedMap<K, V>{
//fields
public:
    static long serialVersionUID;
    SortedMap<K, V>* sm;

//methods
public:
    virtual SortedMap<K, V>* headMap(K );

    virtual SortedMap<K, V>* subMap(K , K );

    virtual SortedMap<K, V>* tailMap(K );

    SynchronizedSortedMap(SortedMap<K, V>* );

    SynchronizedSortedMap(SortedMap<K, V>* , java::lang::Object* );

    Comparator<java::lang::Object*>* comparator();

    K firstKey();

    K lastKey();


};//class SynchronizedSortedMap

}//namespace java
}//namespace util
