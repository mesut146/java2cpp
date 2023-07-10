#pragma once


namespace java{
namespace util{


template <typename E>
class CheckedSet: public CheckedCollection<E>, public java::io::Serializable{
//fields
public:
    static long serialVersionUID;

//methods
public:
    CheckedSet(std::unordered_set<E>* , java::lang::Class<E>* );

    bool equals(java::lang::Object* );

    int hashCode();


};//class CheckedSet

}//namespace java
}//namespace util
