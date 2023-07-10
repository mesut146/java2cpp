#pragma once


namespace java{
namespace util{
namespace concurrent{


template <typename T>
class PrivilegedCallable: public Callable<T>{
//fields
public:
    java::security::AccessControlContext* acc;
    Callable<T>* task;

//methods
public:
    PrivilegedCallable(Callable<T>* );

    T call();


};//class PrivilegedCallable

}//namespace java
}//namespace util
}//namespace concurrent
