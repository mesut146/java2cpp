#pragma once


namespace java{
namespace util{

class Flags
{
//fields
public:
    static Flags* ALTERNATE;
    static Flags* GROUP;
    static Flags* LEADING_SPACE;
    static Flags* LEFT_JUSTIFY;
    static Flags* NONE;
    static Flags* PARENTHESES;
    static Flags* PLUS;
    static Flags* PREVIOUS;
    static Flags* UPPERCASE;
    static Flags* ZERO_PAD;
    int flags;

//methods
public:
    Flags(int );

    Flags* add(Flags* );

    bool contains(Flags* );

    Flags* dup();

    static Flags* parse(std::string* );

    static Flags* parse(wchar_t );

    Flags* remove(Flags* );

    std::string* toString();

    static std::string* toString(Flags* );

    int valueOf();


};//class Flags

}//namespace java
}//namespace util
