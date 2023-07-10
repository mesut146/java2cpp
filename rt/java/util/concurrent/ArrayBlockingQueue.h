#pragma once


namespace java{
namespace util{
namespace concurrent{


template <typename E>
class ArrayBlockingQueue: public java::util::AbstractQueue<E>, public BlockingQueue<E>, public java::io::Serializable{
};//class ArrayBlockingQueue

}//namespace java
}//namespace util
}//namespace concurrent
