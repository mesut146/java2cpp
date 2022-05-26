#pragma once


namespace java{
namespace util{
namespace concurrent{


template <typename V>
class ForkJoinTask: public Future<V>, public java::io::Serializable{
};//class ForkJoinTask

}//namespace java
}//namespace util
}//namespace concurrent
