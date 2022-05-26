#pragma once


namespace java{
namespace util{
namespace regex{

class Script: public CharProperty{
//fields
public:
    java::lang::UnicodeScript* script;

//methods
public:
    Script(java::lang::UnicodeScript* );

    bool isSatisfiedBy(int );


};//class Script

}//namespace java
}//namespace util
}//namespace regex
