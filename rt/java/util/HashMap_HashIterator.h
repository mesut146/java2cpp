#pragma once


namespace java{
namespace util{

class HashIterator
{
//fields
public:
    Node<K, V>* current;
    int expectedModCount;
    int index;
    Node<K, V>* next;
    HashMap* this$0;

//methods
public:
    HashIterator();

    bool hasNext();

    Node<K, V>* nextNode();

    void remove();


};//class HashIterator

}//namespace java
}//namespace util
