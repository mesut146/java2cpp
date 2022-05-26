#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class UnmodifiableNavigableMap: public UnmodifiableSortedMap<K, V>, public NavigableMap<K, V>, public java::io::Serializable{
//fields
public:
    static EmptyNavigableMap<java::lang::Object*, java::lang::Object*>* EMPTY_NAVIGABLE_MAP;
    NavigableMap<K, java::lang::Object*>* nm;
    static long serialVersionUID;

//methods
public:
    UnmodifiableNavigableMap(NavigableMap<K, java::lang::Object*>* );

    Entry<K, V>* ceilingEntry(K );

    K ceilingKey(K );

    NavigableSet<K>* descendingKeySet();

    NavigableMap<K, V>* descendingMap();

    Entry<K, V>* firstEntry();

    Entry<K, V>* floorEntry(K );

    K floorKey(K );

    NavigableMap<K, V>* headMap(K , bool );

    Entry<K, V>* higherEntry(K );

    K higherKey(K );

    Entry<K, V>* lastEntry();

    Entry<K, V>* lowerEntry(K );

    K lowerKey(K );

    virtual NavigableSet<K>* navigableKeySet();

    Entry<K, V>* pollFirstEntry();

    Entry<K, V>* pollLastEntry();

    NavigableMap<K, V>* subMap(K , bool , K , bool );

    NavigableMap<K, V>* tailMap(K , bool );


};//class UnmodifiableNavigableMap

}//namespace java
}//namespace util
