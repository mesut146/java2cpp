#pragma once


namespace java{
namespace util{


template <typename E>
class AbstractSet: public AbstractCollection<E>{
//methods
public:
    AbstractSet();

    virtual bool equals(java::lang::Object* );

    virtual int hashCode();

    virtual bool removeAll(Collection<java::lang::Object*>* );


};//class AbstractSet

}//namespace java
}//namespace util
