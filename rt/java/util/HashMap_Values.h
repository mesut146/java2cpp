#pragma once


namespace java{
namespace util{

class Values: public AbstractCollection<V>{
//fields
public:
    HashMap<K, V>* _parent_ref;
    HashMap* this$0;

//methods
public:
    Values* setRef(HashMap<K, V>* );

    Values();

    void clear();

    bool contains(java::lang::Object* );

    void forEach(java::util::function::Consumer<java::lang::Object*>* );

    Iterator<V>* iterator();

    int size();

    Spliterator<V>* spliterator();


};//class Values

}//namespace java
}//namespace util
