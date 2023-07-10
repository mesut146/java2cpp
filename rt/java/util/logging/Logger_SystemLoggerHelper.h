#pragma once


namespace java{
namespace util{
namespace logging{

class SystemLoggerHelper
{
//fields
public:
    static bool disableCallerCheck;

//methods
public:
    SystemLoggerHelper();

    static bool getBooleanProperty(std::string* );


};//class SystemLoggerHelper

}//namespace java
}//namespace util
}//namespace logging
