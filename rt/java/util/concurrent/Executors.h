#pragma once


namespace java{
namespace util{
namespace concurrent{

class Executors
{
//methods
public:
    Executors();

    static Callable<java::lang::Object*>* callable(java::lang::Runnable* );

    static Callable<java::lang::Object*>* callable(java::security::PrivilegedAction<java::lang::Object*>* );

    static Callable<java::lang::Object*>* callable(java::security::PrivilegedExceptionAction<java::lang::Object*>* );

    template <typename T>
    static Callable<T>* callable(java::lang::Runnable* , T );

    static ThreadFactory* defaultThreadFactory();

    static ExecutorService* newCachedThreadPool();

    static ExecutorService* newCachedThreadPool(ThreadFactory* );

    static ExecutorService* newFixedThreadPool(int );

    static ExecutorService* newFixedThreadPool(int , ThreadFactory* );

    static ScheduledExecutorService* newScheduledThreadPool(int );

    static ScheduledExecutorService* newScheduledThreadPool(int , ThreadFactory* );

    static ExecutorService* newSingleThreadExecutor();

    static ExecutorService* newSingleThreadExecutor(ThreadFactory* );

    static ScheduledExecutorService* newSingleThreadScheduledExecutor();

    static ScheduledExecutorService* newSingleThreadScheduledExecutor(ThreadFactory* );

    static ExecutorService* newWorkStealingPool();

    static ExecutorService* newWorkStealingPool(int );

    template <typename T>
    static Callable<T>* privilegedCallable(Callable<T>* );

    template <typename T>
    static Callable<T>* privilegedCallableUsingCurrentClassLoader(Callable<T>* );

    static ThreadFactory* privilegedThreadFactory();

    static ExecutorService* unconfigurableExecutorService(ExecutorService* );

    static ScheduledExecutorService* unconfigurableScheduledExecutorService(ScheduledExecutorService* );


};//class Executors

}//namespace java
}//namespace util
}//namespace concurrent
