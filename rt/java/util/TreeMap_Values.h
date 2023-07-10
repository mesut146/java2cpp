#pragma once


namespace java{
namespace util{

class Values: public AbstractCollection<V>{
//fields
public:
    TreeMap<K, V>* _parent_ref;
    TreeMap* this$0;

//methods
public:
    Values* setRef(TreeMap<K, V>* );

    Values();

    void clear();

    bool contains(java::lang::Object* );

    Iterator<V>* iterator();

    bool remove(java::lang::Object* );

    int size();

    Spliterator<V>* spliterator();


};//class Values

}//namespace java
}//namespace util
