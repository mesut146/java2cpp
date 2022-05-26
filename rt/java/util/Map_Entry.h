#pragma once


namespace java{
namespace util{

/*interface*/

template <typename K, typename V>
class Entry
{
//methods
public:
    template <typename K, typename V>
    static virtual Comparator<Entry<K, V>*>* comparingByKey() = 0;

    template <typename K, typename V>
    static virtual Comparator<Entry<K, V>*>* comparingByKey(Comparator<java::lang::Object*>* ) = 0;

    template <typename K, typename V>
    static virtual Comparator<Entry<K, V>*>* comparingByValue() = 0;

    template <typename K, typename V>
    static virtual Comparator<Entry<K, V>*>* comparingByValue(Comparator<java::lang::Object*>* ) = 0;

    virtual bool equals(java::lang::Object* ) = 0;

    virtual K getKey() = 0;

    virtual V getValue() = 0;

    virtual int hashCode() = 0;

    virtual V setValue(V ) = 0;


};//class Entry

}//namespace java
}//namespace util
