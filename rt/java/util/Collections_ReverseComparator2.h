#pragma once


namespace java{
namespace util{


template <typename T>
class ReverseComparator2: public Comparator<T>, public java::io::Serializable{
//fields
public:
    static bool $assertionsDisabled;
    Comparator<T>* cmp;
    static long serialVersionUID;

//methods
public:
    ReverseComparator2(Comparator<T>* );

    int compare(T , T );

    bool equals(java::lang::Object* );

    int hashCode();

    Comparator<T>* reversed();


};//class ReverseComparator2

}//namespace java
}//namespace util
