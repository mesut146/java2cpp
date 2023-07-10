#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class SynchronizedNavigableMap: public SynchronizedSortedMap<K, V>, public NavigableMap<K, V>{
//fields
public:
    NavigableMap<K, V>* nm;
    static long serialVersionUID;

//methods
public:
    SynchronizedNavigableMap(NavigableMap<K, V>* );

    SynchronizedNavigableMap(NavigableMap<K, V>* , java::lang::Object* );

    Entry<K, V>* ceilingEntry(K );

    K ceilingKey(K );

    NavigableSet<K>* descendingKeySet();

    NavigableMap<K, V>* descendingMap();

    Entry<K, V>* firstEntry();

    Entry<K, V>* floorEntry(K );

    K floorKey(K );

    SortedMap<K, V>* headMap(K );

    NavigableMap<K, V>* headMap(K , bool );

    Entry<K, V>* higherEntry(K );

    K higherKey(K );

    NavigableSet<K>* keySet();

    Entry<K, V>* lastEntry();

    Entry<K, V>* lowerEntry(K );

    K lowerKey(K );

    NavigableSet<K>* navigableKeySet();

    Entry<K, V>* pollFirstEntry();

    Entry<K, V>* pollLastEntry();

    SortedMap<K, V>* subMap(K , K );

    NavigableMap<K, V>* subMap(K , bool , K , bool );

    SortedMap<K, V>* tailMap(K );

    NavigableMap<K, V>* tailMap(K , bool );


};//class SynchronizedNavigableMap

}//namespace java
}//namespace util
