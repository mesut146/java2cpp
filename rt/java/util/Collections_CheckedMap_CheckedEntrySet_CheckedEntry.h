#pragma once


namespace java{
namespace util{


template <typename K, typename V, typename T>
class CheckedEntry: public Entry<K, V>{
//fields
public:
    Entry<K, V>* e;
    java::lang::Class<T>* valueType;

//methods
public:
    CheckedEntry(Entry<K, V>* , java::lang::Class<T>* );

    std::string* badValueMsg(java::lang::Object* );

    bool equals(java::lang::Object* );

    K getKey();

    V getValue();

    int hashCode();

    V setValue(V );

    std::string* toString();


};//class CheckedEntry

}//namespace java
}//namespace util
