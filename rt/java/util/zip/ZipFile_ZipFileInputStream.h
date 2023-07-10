#pragma once


namespace java{
namespace util{
namespace zip{

class ZipFileInputStream: public java::io::InputStream{
//fields
public:
    ZipFile* _parent_ref;
    long jzentry;
    long pos;
    long rem;
    long size_renamed;
    ZipFile* this$0;
    bool zfisCloseRequested;

//methods
public:
    ZipFileInputStream* setRef(ZipFile* );

    ZipFileInputStream(long );

    int available();

    void close();

    void finalize();

    int read();

    int read(std::vector<char>* , int , int );

    long size();

    long skip(long );


};//class ZipFileInputStream

}//namespace java
}//namespace util
}//namespace zip
