#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class UnmodifiableSortedMap: public UnmodifiableMap<K, V>, public SortedMap<K, V>, public java::io::Serializable{
//fields
public:
    static long serialVersionUID;
    SortedMap<K, java::lang::Object*>* sm;

//methods
public:
    UnmodifiableSortedMap(SortedMap<K, java::lang::Object*>* );

    Comparator<java::lang::Object*>* comparator();

    K firstKey();

    SortedMap<K, V>* headMap(K );

    K lastKey();

    SortedMap<K, V>* subMap(K , K );

    SortedMap<K, V>* tailMap(K );


};//class UnmodifiableSortedMap

}//namespace java
}//namespace util
