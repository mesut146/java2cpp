#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class EmptyMap: public AbstractMap<K, V>, public java::io::Serializable{
//fields
public:
    static long serialVersionUID;

//methods
public:
    EmptyMap();

    V compute(K , java::util::function::BiFunction<java::lang::Object*, java::lang::Object*, java::lang::Object*>* );

    V computeIfAbsent(K , java::util::function::Function<java::lang::Object*, java::lang::Object*>* );

    V computeIfPresent(K , java::util::function::BiFunction<java::lang::Object*, java::lang::Object*, java::lang::Object*>* );

    bool containsKey(java::lang::Object* );

    bool containsValue(java::lang::Object* );

    std::unordered_set<Entry<K, V>*>* entrySet();

    bool equals(java::lang::Object* );

    void forEach(java::util::function::BiConsumer<java::lang::Object*, java::lang::Object*>* );

    V get(java::lang::Object* );

    V getOrDefault(java::lang::Object* , V );

    int hashCode();

    bool isEmpty();

    std::unordered_set<K>* keySet();

    V merge(K , V , java::util::function::BiFunction<java::lang::Object*, java::lang::Object*, java::lang::Object*>* );

    V putIfAbsent(K , V );

    java::lang::Object* readResolve();

    bool remove(java::lang::Object* , java::lang::Object* );

    V replace(K , V );

    bool replace(K , V , V );

    void replaceAll(java::util::function::BiFunction<java::lang::Object*, java::lang::Object*, java::lang::Object*>* );

    int size();

    Collection<V>* values();


};//class EmptyMap

}//namespace java
}//namespace util
