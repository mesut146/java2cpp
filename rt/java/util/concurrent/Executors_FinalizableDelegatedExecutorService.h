#pragma once


namespace java{
namespace util{
namespace concurrent{

class FinalizableDelegatedExecutorService: public DelegatedExecutorService{
//methods
public:
    FinalizableDelegatedExecutorService(ExecutorService* );

    void finalize();


};//class FinalizableDelegatedExecutorService

}//namespace java
}//namespace util
}//namespace concurrent
