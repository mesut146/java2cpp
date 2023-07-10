#pragma once


namespace java{
namespace util{

class AscendingEntrySetView: public EntrySetView{
//fields
public:
    AscendingSubMap<K, V>* _parent_ref;
    AscendingSubMap* this$0;

//methods
public:
    AscendingEntrySetView* setRef(AscendingSubMap<K, V>* );

    AscendingEntrySetView();

    Iterator<Entry<K, V>*>* iterator();


};//class AscendingEntrySetView

}//namespace java
}//namespace util
