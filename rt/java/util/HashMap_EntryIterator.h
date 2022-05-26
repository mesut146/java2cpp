#pragma once


namespace java{
namespace util{

class EntryIterator: public HashIterator, public Iterator<Entry<K, V>*>{
//fields
public:
    HashMap<K, V>* _parent_ref;
    HashMap* this$0;

//methods
public:
    EntryIterator* setRef(HashMap<K, V>* );

    EntryIterator();

    Entry<K, V>* next();


};//class EntryIterator

}//namespace java
}//namespace util
