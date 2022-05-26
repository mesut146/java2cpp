#pragma once


namespace java{
namespace util{
namespace zip{

class DeflaterOutputStream: public java::io::FilterOutputStream{
//fields
public:
    std::vector<char>* buf;
    bool closed;
    Deflater* def;
    bool syncFlush;
    bool usesDefaultDeflater;

//methods
public:
    DeflaterOutputStream(java::io::OutputStream* );

    virtual void close();

    virtual void finish();

    virtual void write(std::vector<char>* , int , int );

    DeflaterOutputStream(java::io::OutputStream* , Deflater* );

    DeflaterOutputStream(java::io::OutputStream* , bool );

    DeflaterOutputStream(java::io::OutputStream* , Deflater* , int );

    DeflaterOutputStream(java::io::OutputStream* , Deflater* , bool );

    DeflaterOutputStream(java::io::OutputStream* , Deflater* , int , bool );

    void deflate();

    void flush();

    void write(int );


};//class DeflaterOutputStream

}//namespace java
}//namespace util
}//namespace zip
