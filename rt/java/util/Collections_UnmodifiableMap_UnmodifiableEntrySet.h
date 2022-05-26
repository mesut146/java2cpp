#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class UnmodifiableEntrySet: public UnmodifiableSet<Entry<K, V>*>{
//fields
public:
    static long serialVersionUID;

//methods
public:
    UnmodifiableEntrySet(std::unordered_set<java::lang::Object*>* );

    bool contains(java::lang::Object* );

    bool containsAll(Collection<java::lang::Object*>* );

    template <typename K, typename V>
    static java::util::function::Consumer<Entry<K, V>*>* entryConsumer(java::util::function::Consumer<java::lang::Object*>* );

    bool equals(java::lang::Object* );

    void forEach(java::util::function::Consumer<java::lang::Object*>* );

    Iterator<Entry<K, V>*>* iterator();

    java::util::stream::Stream<Entry<K, V>*>* parallelStream();

    Spliterator<Entry<K, V>*>* spliterator();

    java::util::stream::Stream<Entry<K, V>*>* stream();

    std::vector<java::lang::Object*>* toArray();

    template <typename T>
    std::vector<T>* toArray(std::vector<T>* );


};//class UnmodifiableEntrySet

}//namespace java
}//namespace util
