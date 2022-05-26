#pragma once


namespace java{
namespace lang{

class String: public java::io::Serializable, public Comparable<String*>, public CharSequence, public java::lang::constant::Constable, public java::lang::constant::ConstantDesc{
};//class String

}//namespace java
}//namespace lang
