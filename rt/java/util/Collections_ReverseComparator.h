#pragma once


namespace java{
namespace util{

class ReverseComparator: public Comparator<java::lang::Comparable<java::lang::Object*>*>, public java::io::Serializable{
//fields
public:
    static ReverseComparator* REVERSE_ORDER;
    static long serialVersionUID;

//methods
public:
    ReverseComparator();

    int compare(java::lang::Comparable<java::lang::Object*>* , java::lang::Comparable<java::lang::Object*>* );

    java::lang::Object* readResolve();

    Comparator<java::lang::Comparable<java::lang::Object*>*>* reversed();


};//class ReverseComparator

}//namespace java
}//namespace util
