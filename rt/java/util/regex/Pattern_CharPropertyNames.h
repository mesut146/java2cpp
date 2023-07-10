#pragma once


namespace java{
namespace util{
namespace regex{

class CharPropertyNames
{
//fields
public:
    static java::util::HashMap<std::string*, CharPropertyFactory*>* map;

//methods
public:
    CharPropertyNames();

    static CharProperty* charPropertyFor(std::string* );

    static void defCategory(std::string* , int );

    static void defClone(std::string* , CloneableProperty* );

    static void defCtype(std::string* , int );

    static void defRange(std::string* , int , int );


};//class CharPropertyNames

}//namespace java
}//namespace util
}//namespace regex
