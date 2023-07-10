#pragma once


namespace java{
namespace util{
namespace logging{

class LoggerBundle
{
//fields
public:
    std::string* resourceBundleName;
    java::util::ResourceBundle* userBundle;

//methods
public:
    LoggerBundle(std::string* , java::util::ResourceBundle* );

    static LoggerBundle* get(std::string* , java::util::ResourceBundle* );

    bool isSystemBundle();


};//class LoggerBundle

}//namespace java
}//namespace util
}//namespace logging
