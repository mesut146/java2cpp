#pragma once


namespace java{
namespace util{

class ValueIterator: public HashIterator, public Iterator<V>{
//fields
public:
    HashMap<K, V>* _parent_ref;
    HashMap* this$0;

//methods
public:
    ValueIterator* setRef(HashMap<K, V>* );

    ValueIterator();

    V next();


};//class ValueIterator

}//namespace java
}//namespace util
