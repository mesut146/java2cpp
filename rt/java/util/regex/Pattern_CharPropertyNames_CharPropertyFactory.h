#pragma once


namespace java{
namespace util{
namespace regex{

class CharPropertyFactory
{
//methods
public:
    CharPropertyFactory();

    virtual CharProperty* make() = 0;


};//class CharPropertyFactory

}//namespace java
}//namespace util
}//namespace regex
