#pragma once


namespace java {
    namespace util {
        namespace concurrent {

/*interface*/
            class ExecutorService : public Executor {
//methods
            public:
                virtual bool awaitTermination(long, TimeUnit *) = 0;

                template<typename T>
                virtual std::vector<Future < T>*>*
                invokeAll(java::util::Collection<java::lang::Object *>
                * ) = 0;

                template<typename T>
                virtual std::vector<Future < T>*>*
                invokeAll(java::util::Collection<java::lang::Object *>
                * , long , TimeUnit* ) = 0;

                template<typename T>
                virtual T invokeAny(java::util::Collection<java::lang::Object *> *) = 0;

                template<typename T>
                virtual T invokeAny(java::util::Collection<java::lang::Object *> *, long, TimeUnit *) = 0;

                virtual bool isShutdown() = 0;

                virtual bool isTerminated() = 0;

                virtual void shutdown() = 0;

                virtual std::vector<java::lang::Runnable *> *shutdownNow() = 0;

                template<typename T>
                virtual Future <T> *submit(Callable <T> *) = 0;

                virtual Future<java::lang::Object *> *submit(java::lang::Runnable *) = 0;

                template<typename T>
                virtual Future <T> *submit(java::lang::Runnable *, T) = 0;


            };//class ExecutorService

        }//namespace java
    }//namespace util
}//namespace concurrent
