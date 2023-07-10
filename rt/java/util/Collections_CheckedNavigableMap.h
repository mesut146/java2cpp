#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class CheckedNavigableMap: public CheckedSortedMap<K, V>, public NavigableMap<K, V>, public java::io::Serializable{
//fields
public:
    NavigableMap<K, V>* nm;
    static long serialVersionUID;

//methods
public:
    CheckedNavigableMap(NavigableMap<K, V>* , java::lang::Class<K>* , java::lang::Class<V>* );

    Entry<K, V>* ceilingEntry(K );

    K ceilingKey(K );

    Comparator<java::lang::Object*>* comparator();

    NavigableSet<K>* descendingKeySet();

    NavigableMap<K, V>* descendingMap();

    Entry<K, V>* firstEntry();

    K firstKey();

    Entry<K, V>* floorEntry(K );

    K floorKey(K );

    NavigableMap<K, V>* headMap(K );

    NavigableMap<K, V>* headMap(K , bool );

    Entry<K, V>* higherEntry(K );

    K higherKey(K );

    NavigableSet<K>* keySet();

    Entry<K, V>* lastEntry();

    K lastKey();

    Entry<K, V>* lowerEntry(K );

    K lowerKey(K );

    NavigableSet<K>* navigableKeySet();

    Entry<K, V>* pollFirstEntry();

    Entry<K, V>* pollLastEntry();

    NavigableMap<K, V>* subMap(K , K );

    NavigableMap<K, V>* subMap(K , bool , K , bool );

    NavigableMap<K, V>* tailMap(K );

    NavigableMap<K, V>* tailMap(K , bool );


};//class CheckedNavigableMap

}//namespace java
}//namespace util
