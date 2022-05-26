#pragma once


namespace java{
namespace util{
namespace concurrent{

class AbstractExecutorService: public ExecutorService{
//methods
public:
    template <typename T>
    virtual std::vector<Future<T>*>* invokeAll(java::util::Collection<java::lang::Object*>* );

    template <typename T>
    virtual std::vector<Future<T>*>* invokeAll(java::util::Collection<java::lang::Object*>* , long , TimeUnit* );

    template <typename T>
    virtual T invokeAny(java::util::Collection<java::lang::Object*>* );

    template <typename T>
    virtual T invokeAny(java::util::Collection<java::lang::Object*>* , long , TimeUnit* );

    virtual Future<java::lang::Object*>* submit(java::lang::Runnable* );

    template <typename T>
    virtual Future<T>* submit(Callable<T>* );

    template <typename T>
    virtual Future<T>* submit(java::lang::Runnable* , T );


};//class AbstractExecutorService

}//namespace java
}//namespace util
}//namespace concurrent
