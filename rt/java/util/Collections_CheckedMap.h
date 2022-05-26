#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class CheckedMap: public Map<K, V>, public java::io::Serializable{
//fields
public:
    std::unordered_set<Entry<K, V>*>* entrySet_renamed;
    java::lang::Class<K>* keyType;
    Map<K, V>* m;
    static long serialVersionUID;
    java::lang::Class<V>* valueType;

//methods
public:
    CheckedMap(Map<K, V>* , java::lang::Class<K>* , java::lang::Class<V>* );

    std::string* badKeyMsg(java::lang::Object* );

    std::string* badValueMsg(java::lang::Object* );

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

    java::util::function::BiFunction<java::lang::Object*, java::lang::Object*, java::lang::Object*>* typeCheck(java::util::function::BiFunction<java::lang::Object*, java::lang::Object*, java::lang::Object*>* );

    void typeCheck(java::lang::Object* , java::lang::Object* );

    Collection<V>* values();


};//class CheckedMap

}//namespace java
}//namespace util
