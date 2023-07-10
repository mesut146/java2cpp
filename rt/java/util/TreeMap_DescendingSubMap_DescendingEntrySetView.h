#pragma once


namespace java{
namespace util{

class DescendingEntrySetView: public EntrySetView{
//fields
public:
    DescendingSubMap<K, V>* _parent_ref;
    DescendingSubMap* this$0;

//methods
public:
    DescendingEntrySetView* setRef(DescendingSubMap<K, V>* );

    DescendingEntrySetView();

    Iterator<Entry<K, V>*>* iterator();


};//class DescendingEntrySetView

}//namespace java
}//namespace util
