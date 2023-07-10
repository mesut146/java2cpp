#pragma once


namespace java{
namespace util{
namespace zip{

class XEntry
{
//fields
public:
    ZipEntry* entry;
    long offset;

//methods
public:
    XEntry(ZipEntry* , long );


};//class XEntry

}//namespace java
}//namespace util
}//namespace zip
