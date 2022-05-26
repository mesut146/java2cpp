#pragma once


namespace java{
namespace util{

class DescendingKeyIterator: public PrivateEntryIterator<K>{
//fields
public:
    TreeMap<K, V>* _parent_ref;
    TreeMap* this$0;

//methods
public:
    DescendingKeyIterator* setRef(TreeMap<K, V>* );

    DescendingKeyIterator(Entry<K, V>* );

    K next();

    void remove();


};//class DescendingKeyIterator

}//namespace java
}//namespace util
