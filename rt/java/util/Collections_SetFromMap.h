#pragma once


namespace java{
namespace util{


template <typename E>
class SetFromMap: public AbstractSet<E>, public java::io::Serializable{
//fields
public:
    Map<E, java::lang::Boolean*>* m;
    std::unordered_set<E>* s;
    static long serialVersionUID;

//methods
public:
    SetFromMap(Map<E, java::lang::Boolean*>* );

    bool add(E );

    void clear();

    bool contains(java::lang::Object* );

    bool containsAll(Collection<java::lang::Object*>* );

    bool equals(java::lang::Object* );

    void forEach(java::util::function::Consumer<java::lang::Object*>* );

    int hashCode();

    bool isEmpty();

    Iterator<E>* iterator();

    java::util::stream::Stream<E>* parallelStream();

    void readObject(java::io::ObjectInputStream* );

    bool remove(java::lang::Object* );

    bool removeAll(Collection<java::lang::Object*>* );

    bool removeIf(java::util::function::Predicate<java::lang::Object*>* );

    bool retainAll(Collection<java::lang::Object*>* );

    int size();

    Spliterator<E>* spliterator();

    java::util::stream::Stream<E>* stream();

    std::vector<java::lang::Object*>* toArray();

    template <typename T>
    std::vector<T>* toArray(std::vector<T>* );

    std::string* toString();


};//class SetFromMap

}//namespace java
}//namespace util
