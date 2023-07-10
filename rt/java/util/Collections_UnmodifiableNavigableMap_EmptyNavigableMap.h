#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class EmptyNavigableMap: public UnmodifiableNavigableMap<K, V>, public java::io::Serializable{
//fields
public:
    static long serialVersionUID;

//methods
public:
    EmptyNavigableMap();

    NavigableSet<K>* navigableKeySet();

    java::lang::Object* readResolve();


};//class EmptyNavigableMap

}//namespace java
}//namespace util
