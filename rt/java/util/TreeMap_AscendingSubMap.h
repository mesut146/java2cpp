#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class AscendingSubMap: public NavigableSubMap<K, V>{
//fields
public:
    static long serialVersionUID;

//methods
public:
    AscendingSubMap(TreeMap<K, V>* , bool , K , bool , bool , K , bool );

    Comparator<java::lang::Object*>* comparator();

    Iterator<K>* descendingKeyIterator();

    NavigableMap<K, V>* descendingMap();

    std::unordered_set<Entry<K, V>*>* entrySet();

    NavigableMap<K, V>* headMap(K , bool );

    Iterator<K>* keyIterator();

    Spliterator<K>* keySpliterator();

    Entry<K, V>* subCeiling(K );

    Entry<K, V>* subFloor(K );

    Entry<K, V>* subHigher(K );

    Entry<K, V>* subHighest();

    Entry<K, V>* subLower(K );

    Entry<K, V>* subLowest();

    NavigableMap<K, V>* subMap(K , bool , K , bool );

    NavigableMap<K, V>* tailMap(K , bool );


};//class AscendingSubMap

}//namespace java
}//namespace util
