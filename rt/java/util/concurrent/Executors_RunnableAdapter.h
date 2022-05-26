#pragma once


namespace java{
namespace util{
namespace concurrent{


template <typename T>
class RunnableAdapter: public Callable<T>{
//fields
public:
    T result;
    java::lang::Runnable* task;

//methods
public:
    RunnableAdapter(java::lang::Runnable* , T );

    T call();


};//class RunnableAdapter

}//namespace java
}//namespace util
}//namespace concurrent
