#pragma once


namespace java{
namespace util{

class EntrySet: public AbstractSet<Entry<K, V>*>{
//fields
public:
    TreeMap<K, V>* _parent_ref;
    TreeMap* this$0;

//methods
public:
    EntrySet* setRef(TreeMap<K, V>* );

    EntrySet();

    void clear();

    bool contains(java::lang::Object* );

    Iterator<Entry<K, V>*>* iterator();

    bool remove(java::lang::Object* );

    int size();

    Spliterator<Entry<K, V>*>* spliterator();


};//class EntrySet

}//namespace java
}//namespace util
