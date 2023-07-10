#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class Node: public Entry<K, V>{
//fields
public:
    int hash;
    K key;
    Node<K, V>* next;
    V value;

//methods
public:
    Node(int , K , V , Node<K, V>* );

    bool equals(java::lang::Object* );

    K getKey();

    V getValue();

    int hashCode();

    V setValue(V );

    std::string* toString();


};//class Node

}//namespace java
}//namespace util
