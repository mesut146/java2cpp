#pragma once


namespace java{
namespace util{
namespace concurrent{

class DelegatedExecutorService: public AbstractExecutorService{
//fields
public:
    ExecutorService* e;

//methods
public:
    DelegatedExecutorService(ExecutorService* );

    bool awaitTermination(long , TimeUnit* );

    void execute(java::lang::Runnable* );

    template <typename T>
    std::vector<Future<T>*>* invokeAll(java::util::Collection<java::lang::Object*>* );

    template <typename T>
    std::vector<Future<T>*>* invokeAll(java::util::Collection<java::lang::Object*>* , long , TimeUnit* );

    template <typename T>
    T invokeAny(java::util::Collection<java::lang::Object*>* );

    template <typename T>
    T invokeAny(java::util::Collection<java::lang::Object*>* , long , TimeUnit* );

    bool isShutdown();

    bool isTerminated();

    void shutdown();

    std::vector<java::lang::Runnable*>* shutdownNow();

    Future<java::lang::Object*>* submit(java::lang::Runnable* );

    template <typename T>
    Future<T>* submit(Callable<T>* );

    template <typename T>
    Future<T>* submit(java::lang::Runnable* , T );


};//class DelegatedExecutorService

}//namespace java
}//namespace util
}//namespace concurrent
