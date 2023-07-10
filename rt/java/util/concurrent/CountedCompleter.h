#pragma once


namespace java{
namespace util{
namespace concurrent{


template <typename T>
class CountedCompleter: public ForkJoinTask<T>{
};//class CountedCompleter

}//namespace java
}//namespace util
}//namespace concurrent
