#pragma once


namespace java{
namespace util{

class KeyIterator: public PrivateEntryIterator<K>{
//fields
public:
    TreeMap<K, V>* _parent_ref;
    TreeMap* this$0;

//methods
public:
    KeyIterator* setRef(TreeMap<K, V>* );

    KeyIterator(Entry<K, V>* );

    K next();


};//class KeyIterator

}//namespace java
}//namespace util
