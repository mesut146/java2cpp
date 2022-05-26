#pragma once


namespace java{
namespace util{

class DescendingSubMapEntryIterator: public SubMapIterator<Entry<K, V>*>{
//fields
public:
    NavigableSubMap<K, V>* _parent_ref;
    NavigableSubMap* this$0;

//methods
public:
    DescendingSubMapEntryIterator* setRef(NavigableSubMap<K, V>* );

    DescendingSubMapEntryIterator(Entry<K, V>* , Entry<K, V>* );

    Entry<K, V>* next();

    void remove();


};//class DescendingSubMapEntryIterator

}//namespace java
}//namespace util
