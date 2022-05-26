#pragma once


namespace java{
namespace util{

class SubMap: public AbstractMap<K, V>, public SortedMap<K, V>, public java::io::Serializable{
//fields
public:
    TreeMap<K, V>* _parent_ref;
    K fromKey;
    bool fromStart;
    static long serialVersionUID;
    TreeMap* this$0;
    bool toEnd;
    K toKey;

//methods
public:
    SubMap* setRef(TreeMap<K, V>* );

    SubMap();

    Comparator<java::lang::Object*>* comparator();

    std::unordered_set<Entry<K, V>*>* entrySet();

    K firstKey();

    SortedMap<K, V>* headMap(K );

    K lastKey();

    java::lang::Object* readResolve();

    SortedMap<K, V>* subMap(K , K );

    SortedMap<K, V>* tailMap(K );


};//class SubMap

}//namespace java
}//namespace util
