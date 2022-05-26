#pragma once


namespace java{
namespace util{
namespace jar{

class Name
{
//fields
public:
    static Name* CLASS_PATH;
    static Name* CONTENT_TYPE;
    static Name* EXTENSION_INSTALLATION;
    static Name* EXTENSION_LIST;
    static Name* EXTENSION_NAME;
    static Name* IMPLEMENTATION_TITLE;
    static Name* IMPLEMENTATION_URL;
    static Name* IMPLEMENTATION_VENDOR;
    static Name* IMPLEMENTATION_VENDOR_ID;
    static Name* IMPLEMENTATION_VERSION;
    static Name* MAIN_CLASS;
    static Name* MANIFEST_VERSION;
    static Name* SEALED;
    static Name* SIGNATURE_VERSION;
    static Name* SPECIFICATION_TITLE;
    static Name* SPECIFICATION_VENDOR;
    static Name* SPECIFICATION_VERSION;
    int hashCode_renamed;
    std::string* name;

//methods
public:
    Name(std::string* );

    bool equals(java::lang::Object* );

    int hashCode();

    static bool isAlpha(wchar_t );

    static bool isDigit(wchar_t );

    static bool isValid(std::string* );

    static bool isValid(wchar_t );

    std::string* toString();


};//class Name

}//namespace java
}//namespace util
}//namespace jar
