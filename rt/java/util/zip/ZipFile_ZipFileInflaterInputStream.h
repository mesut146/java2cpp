#pragma once


namespace java{
namespace util{
namespace zip{

class ZipFileInflaterInputStream: public InflaterInputStream{
//fields
public:
    ZipFile* _parent_ref;
    bool closeRequested;
    bool eof;
    ZipFile* this$0;
    ZipFileInputStream* zfin;

//methods
public:
    ZipFileInflaterInputStream* setRef(ZipFile* );

    ZipFileInflaterInputStream(ZipFileInputStream* , Inflater* , int );

    int available();

    void close();

    void fill();

    void finalize();


};//class ZipFileInflaterInputStream

}//namespace java
}//namespace util
}//namespace zip
