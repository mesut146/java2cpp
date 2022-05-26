#pragma once


namespace java{
namespace util{

class EntrySetView: public AbstractSet<Entry<K, V>*>{
//fields
public:
    int size_renamed;
    int sizeModCount;
    NavigableSubMap* this$0;

//methods
public:
    EntrySetView();

    bool contains(java::lang::Object* );

    bool isEmpty();

    bool remove(java::lang::Object* );

    int size();


};//class EntrySetView

}//namespace java
}//namespace util
