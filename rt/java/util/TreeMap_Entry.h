#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class Entry: public Entry<K, V>{
//fields
public:
    bool color;
    K key;
    Entry<K, V>* left;
    Entry<K, V>* parent;
    Entry<K, V>* right;
    V value;

//methods
public:
    Entry(K , V , Entry<K, V>* );

    bool equals(java::lang::Object* );

    K getKey();

    V getValue();

    int hashCode();

    V setValue(V );

    std::string* toString();


};//class Entry

}//namespace java
}//namespace util
