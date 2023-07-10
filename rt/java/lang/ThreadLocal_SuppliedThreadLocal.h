#pragma once


namespace java{
namespace lang{


template <typename T>
class SuppliedThreadLocal: public ThreadLocal<T>{
//fields
public:
    java::util::function::Supplier<Object*>* supplier;

//methods
public:
    SuppliedThreadLocal(java::util::function::Supplier<Object*>* );

    T initialValue();


};//class SuppliedThreadLocal

}//namespace java
}//namespace lang
