#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class SynchronizedMap: public Map<K, V>, public java::io::Serializable{
//fields
public:
    std::unordered_set<Entry<K, V>*>* entrySet_renamed;
    std::unordered_set<K>* keySet_renamed;
    Map<K, V>* m;
    java::lang::Object* mutex;
    static long serialVersionUID;
    Collection<V>* values_renamed;

//methods
public:
    SynchronizedMap(Map<K, V>* );

    SynchronizedMap(Map<K, V>* , java::lang::Object* );

    void clear();

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

    V put(K , V );

    void putAll(Map<java::lang::Object*, java::lang::Object*>* );

    V putIfAbsent(K , V );

    V remove(java::lang::Object* );

    bool remove(java::lang::Object* , java::lang::Object* );

    V replace(K , V );

    bool replace(K , V , V );

    void replaceAll(java::util::function::BiFunction<java::lang::Object*, java::lang::Object*, java::lang::Object*>* );

    int size();

    std::string* toString();

    Collection<V>* values();

    void writeObject(java::io::ObjectOutputStream* );


};//class SynchronizedMap

}//namespace java
}//namespace util
