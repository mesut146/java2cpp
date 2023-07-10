#pragma once


namespace java{
namespace util{
namespace regex{

class CloneableProperty: public CharProperty, public java::lang::Cloneable{
//methods
public:
    CloneableProperty();

    CloneableProperty* clone();


};//class CloneableProperty

}//namespace java
}//namespace util
}//namespace regex
