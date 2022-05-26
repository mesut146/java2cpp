#pragma once


namespace java{
namespace util{


template <typename E>
class UnmodifiableSet: public UnmodifiableCollection<E>, public java::io::Serializable{
//fields
public:
    static long serialVersionUID;

//methods
public:
    virtual bool equals(java::lang::Object* );

    UnmodifiableSet(std::unordered_set<java::lang::Object*>* );

    int hashCode();


};//class UnmodifiableSet

}//namespace java
}//namespace util
