#pragma once


namespace java{
namespace util{


template <typename E>
class ArrayList: public AbstractList<E>, public List<E>, public RandomAccess, public java::lang::Cloneable, public java::io::Serializable{
};//class ArrayList

}//namespace java
}//namespace util
