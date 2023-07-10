#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class CheckedSortedMap: public CheckedMap<K, V>, public SortedMap<K, V>, public java::io::Serializable{
//fields
public:
    static long serialVersionUID;
    SortedMap<K, V>* sm;

//methods
public:
    virtual Comparator<java::lang::Object*>* comparator();

    virtual K firstKey();

    virtual SortedMap<K, V>* headMap(K );

    virtual K lastKey();

    virtual SortedMap<K, V>* subMap(K , K );

    virtual SortedMap<K, V>* tailMap(K );

    CheckedSortedMap(SortedMap<K, V>* , java::lang::Class<K>* , java::lang::Class<V>* );


};//class CheckedSortedMap

}//namespace java
}//namespace util
