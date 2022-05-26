#pragma once


namespace java{
namespace lang{

class Subset
{
//fields
public:
    std::string* name;

//methods
public:
    Subset(std::string* );

    bool equals(Object* );

    int hashCode();

    std::string* toString();


};//class Subset

}//namespace java
}//namespace lang
