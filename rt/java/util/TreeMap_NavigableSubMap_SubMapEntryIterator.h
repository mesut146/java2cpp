#pragma once


namespace java{
namespace util{

class SubMapEntryIterator: public SubMapIterator<Entry<K, V>*>{
//fields
public:
    NavigableSubMap<K, V>* _parent_ref;
    NavigableSubMap* this$0;

//methods
public:
    SubMapEntryIterator* setRef(NavigableSubMap<K, V>* );

    SubMapEntryIterator(Entry<K, V>* , Entry<K, V>* );

    Entry<K, V>* next();

    void remove();


};//class SubMapEntryIterator

}//namespace java
}//namespace util
