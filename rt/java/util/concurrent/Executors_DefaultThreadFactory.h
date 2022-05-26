#pragma once


namespace java{
namespace util{
namespace concurrent{

class DefaultThreadFactory: public ThreadFactory{
//fields
public:
    java::lang::ThreadGroup* group;
    std::string* namePrefix;
    static java::util::concurrent::atomic::AtomicInteger* poolNumber;
    java::util::concurrent::atomic::AtomicInteger* threadNumber;

//methods
public:
    DefaultThreadFactory();

    virtual java::lang::Thread* newThread(java::lang::Runnable* );


};//class DefaultThreadFactory

}//namespace java
}//namespace util
}//namespace concurrent
