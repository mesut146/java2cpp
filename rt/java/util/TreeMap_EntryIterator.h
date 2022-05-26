#pragma once


namespace java{
namespace util{

class EntryIterator: public PrivateEntryIterator<Entry<K, V>*>{
//fields
public:
    TreeMap<K, V>* _parent_ref;
    TreeMap* this$0;

//methods
public:
    EntryIterator* setRef(TreeMap<K, V>* );

    EntryIterator(Entry<K, V>* );

    Entry<K, V>* next();


};//class EntryIterator

}//namespace java
}//namespace util
