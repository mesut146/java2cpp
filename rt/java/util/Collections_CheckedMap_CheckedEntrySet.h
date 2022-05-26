#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class CheckedEntrySet
{
//fields
public:
    std::unordered_set<Entry<K, V>*>* s;
    java::lang::Class<V>* valueType;

//methods
public:
    CheckedEntrySet(std::unordered_set<Entry<K, V>*>* , java::lang::Class<V>* );

    bool add(Entry<K, V>* );

    bool addAll(Collection<java::lang::Object*>* );

    bool batchRemove(Collection<java::lang::Object*>* , bool );

    template <typename K, typename V, typename T>
    static CheckedEntry<K, V, T>* checkedEntry(Entry<K, V>* , java::lang::Class<T>* );

    void clear();

    bool contains(java::lang::Object* );

    bool containsAll(Collection<java::lang::Object*>* );

    bool equals(java::lang::Object* );

    int hashCode();

    bool isEmpty();

    Iterator<Entry<K, V>*>* iterator();

    bool remove(java::lang::Object* );

    bool removeAll(Collection<java::lang::Object*>* );

    bool retainAll(Collection<java::lang::Object*>* );

    int size();

    std::vector<java::lang::Object*>* toArray();

    template <typename T>
    std::vector<T>* toArray(std::vector<T>* );

    std::string* toString();


};//class CheckedEntrySet

}//namespace java
}//namespace util
