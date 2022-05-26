#pragma once


namespace java{
namespace util{

/*interface*/
class FormatString
{
//methods
public:
    virtual int index() = 0;

    virtual void print(java::lang::Object* , Locale* ) = 0;

    virtual std::string* toString() = 0;


};//class FormatString

}//namespace java
}//namespace util
