#pragma once


namespace java{
namespace util{

class KeySet: public AbstractSet<K>{
//fields
public:
    HashMap<K, V>* _parent_ref;
    HashMap* this$0;

//methods
public:
    KeySet* setRef(HashMap<K, V>* );

    KeySet();

    void clear();

    bool contains(java::lang::Object* );

    void forEach(java::util::function::Consumer<java::lang::Object*>* );

    Iterator<K>* iterator();

    bool remove(java::lang::Object* );

    int size();

    Spliterator<K>* spliterator();


};//class KeySet

}//namespace java
}//namespace util
