#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class UnmodifiableEntry: public Entry<K, V>{
//fields
public:
    Entry<java::lang::Object*, java::lang::Object*>* e;

//methods
public:
    UnmodifiableEntry(Entry<java::lang::Object*, java::lang::Object*>* );

    bool equals(java::lang::Object* );

    K getKey();

    V getValue();

    int hashCode();

    V setValue(V );

    std::string* toString();


};//class UnmodifiableEntry

}//namespace java
}//namespace util
