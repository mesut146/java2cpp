#pragma once


namespace java{
namespace util{
namespace zip{

class ZipEntryIterator: public java::util::Enumeration<ZipEntry*>, public java::util::Iterator<ZipEntry*>{
//fields
public:
    ZipFile* _parent_ref;
    int i;
    ZipFile* this$0;

//methods
public:
    ZipEntryIterator* setRef(ZipFile* );

    ZipEntryIterator();

    bool hasMoreElements();

    bool hasNext();

    ZipEntry* next();

    ZipEntry* nextElement();


};//class ZipEntryIterator

}//namespace java
}//namespace util
}//namespace zip
