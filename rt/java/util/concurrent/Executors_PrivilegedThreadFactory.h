#pragma once


namespace java{
namespace util{
namespace concurrent{

class PrivilegedThreadFactory: public DefaultThreadFactory{
//fields
public:
    java::security::AccessControlContext* acc;
    java::lang::ClassLoader* ccl;

//methods
public:
    PrivilegedThreadFactory();

    java::lang::Thread* newThread(java::lang::Runnable* );


};//class PrivilegedThreadFactory

}//namespace java
}//namespace util
}//namespace concurrent
