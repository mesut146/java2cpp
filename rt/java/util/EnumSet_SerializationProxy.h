#pragma once


namespace java{
namespace util{


template <typename E>
class SerializationProxy: public java::io::Serializable{
//fields
public:
    java::lang::Class<E>* elementType;
    std::vector<java::lang::Enum<java::lang::Object*>*>* elements;
    static long serialVersionUID;

//methods
public:
    SerializationProxy(EnumSet<E>* );

    java::lang::Object* readResolve();


};//class SerializationProxy

}//namespace java
}//namespace util
