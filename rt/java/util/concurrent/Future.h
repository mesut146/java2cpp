#pragma once


namespace java{
namespace util{
namespace concurrent{

/*interface*/

template <typename V>
class Future
{
//methods
public:
    virtual bool cancel(bool ) = 0;

    virtual V get() = 0;

    virtual V get(long , TimeUnit* ) = 0;

    virtual bool isCancelled() = 0;

    virtual bool isDone() = 0;


};//class Future

}//namespace java
}//namespace util
}//namespace concurrent
