#pragma once


namespace java{
namespace util{

class KeyIterator: public HashIterator, public Iterator<K>{
//fields
public:
    HashMap<K, V>* _parent_ref;
    HashMap* this$0;

//methods
public:
    KeyIterator* setRef(HashMap<K, V>* );

    KeyIterator();

    K next();


};//class KeyIterator

}//namespace java
}//namespace util
