#pragma once


namespace java{
namespace util{
namespace concurrent{


template <typename T>
class PrivilegedCallableUsingCurrentClassLoader: public Callable<T>{
//fields
public:
    java::security::AccessControlContext* acc;
    java::lang::ClassLoader* ccl;
    Callable<T>* task;

//methods
public:
    PrivilegedCallableUsingCurrentClassLoader(Callable<T>* );

    T call();


};//class PrivilegedCallableUsingCurrentClassLoader

}//namespace java
}//namespace util
}//namespace concurrent
