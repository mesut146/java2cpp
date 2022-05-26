#pragma once


namespace java{
namespace util{


template <typename E>
class HashSet: public AbstractSet<E>, public Set<E>, public java::lang::Cloneable, public java::io::Serializable{
//fields
public:
    static java::lang::Object* PRESENT;
    HashMap<E, java::lang::Object*>* map;
    static long serialVersionUID;

//methods
public:
    HashSet();

    HashSet(Collection<java::lang::Object*>* );

    HashSet(int );

    HashSet(int , float );

    HashSet(int , float , bool );

    bool add(E );

    void clear();

    java::lang::Object* clone();

    bool contains(java::lang::Object* );

    bool isEmpty();

    Iterator<E>* iterator();

    void readObject(java::io::ObjectInputStream* );

    bool remove(java::lang::Object* );

    int size();

    Spliterator<E>* spliterator();

    void writeObject(java::io::ObjectOutputStream* );


};//class HashSet

}//namespace java
}//namespace util
