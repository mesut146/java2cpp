#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class NavigableSubMap: public AbstractMap<K, V>, public NavigableMap<K, V>, public java::io::Serializable{
//fields
public:
    NavigableMap<K, V>* descendingMapView;
    EntrySetView* entrySetView;
    bool fromStart;
    K hi;
    bool hiInclusive;
    K lo;
    bool loInclusive;
    TreeMap<K, V>* m;
    KeySet<K>* navigableKeySetView;
    static long serialVersionUID;
    bool toEnd;

//methods
public:
    NavigableSubMap(TreeMap<K, V>* , bool , K , bool , bool , K , bool );

    virtual Iterator<K>* descendingKeyIterator() = 0;

    virtual Iterator<K>* keyIterator() = 0;

    virtual Spliterator<K>* keySpliterator() = 0;

    virtual Entry<K, V>* subCeiling(K ) = 0;

    virtual Entry<K, V>* subFloor(K ) = 0;

    virtual Entry<K, V>* subHigher(K ) = 0;

    virtual Entry<K, V>* subHighest() = 0;

    virtual Entry<K, V>* subLower(K ) = 0;

    virtual Entry<K, V>* subLowest() = 0;

    Entry<K, V>* absCeiling(K );

    Entry<K, V>* absFloor(K );

    Entry<K, V>* absHighFence();

    Entry<K, V>* absHigher(K );

    Entry<K, V>* absHighest();

    Entry<K, V>* absLowFence();

    Entry<K, V>* absLower(K );

    Entry<K, V>* absLowest();

    Entry<K, V>* ceilingEntry(K );

    K ceilingKey(K );

    bool containsKey(java::lang::Object* );

    NavigableSet<K>* descendingKeySet();

    Entry<K, V>* firstEntry();

    K firstKey();

    Entry<K, V>* floorEntry(K );

    K floorKey(K );

    V get(java::lang::Object* );

    SortedMap<K, V>* headMap(K );

    Entry<K, V>* higherEntry(K );

    K higherKey(K );

    bool inClosedRange(java::lang::Object* );

    bool inRange(java::lang::Object* );

    bool inRange(java::lang::Object* , bool );

    bool isEmpty();

    std::unordered_set<K>* keySet();

    Entry<K, V>* lastEntry();

    K lastKey();

    Entry<K, V>* lowerEntry(K );

    K lowerKey(K );

    NavigableSet<K>* navigableKeySet();

    Entry<K, V>* pollFirstEntry();

    Entry<K, V>* pollLastEntry();

    V put(K , V );

    V remove(java::lang::Object* );

    int size();

    SortedMap<K, V>* subMap(K , K );

    SortedMap<K, V>* tailMap(K );

    bool tooHigh(java::lang::Object* );

    bool tooLow(java::lang::Object* );


};//class NavigableSubMap

}//namespace java
}//namespace util
