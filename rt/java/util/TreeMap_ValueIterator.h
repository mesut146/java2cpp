#pragma once


namespace java{
namespace util{

class ValueIterator: public PrivateEntryIterator<V>{
//fields
public:
    TreeMap<K, V>* _parent_ref;
    TreeMap* this$0;

//methods
public:
    ValueIterator* setRef(TreeMap<K, V>* );

    ValueIterator(Entry<K, V>* );

    V next();


};//class ValueIterator

}//namespace java
}//namespace util
