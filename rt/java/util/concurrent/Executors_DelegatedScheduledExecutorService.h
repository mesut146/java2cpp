#pragma once


namespace java{
namespace util{
namespace concurrent{

class DelegatedScheduledExecutorService: public DelegatedExecutorService, public ScheduledExecutorService{
//fields
public:
    ScheduledExecutorService* e;

//methods
public:
    DelegatedScheduledExecutorService(ScheduledExecutorService* );

    ScheduledFuture<java::lang::Object*>* schedule(java::lang::Runnable* , long , TimeUnit* );

    template <typename V>
    ScheduledFuture<V>* schedule(Callable<V>* , long , TimeUnit* );

    ScheduledFuture<java::lang::Object*>* scheduleAtFixedRate(java::lang::Runnable* , long , long , TimeUnit* );

    ScheduledFuture<java::lang::Object*>* scheduleWithFixedDelay(java::lang::Runnable* , long , long , TimeUnit* );


};//class DelegatedScheduledExecutorService

}//namespace java
}//namespace util
}//namespace concurrent
