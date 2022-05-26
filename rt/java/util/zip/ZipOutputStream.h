#pragma once


namespace java{
namespace util{
namespace zip{

class ZipOutputStream: public DeflaterOutputStream, public ZipConstants{
//fields
public:
    static int DEFLATED;
    static int STORED;
    bool closed;
    std::vector<char>* comment;
    CRC32* crc;
    XEntry* current;
    bool finished;
    static bool inhibitZip64;
    long locoff;
    int method;
    std::unordered_set<std::string*>* names;
    long written;
    java::util::Vector<XEntry*>* xentries;
    ZipCoder* zc;

//methods
public:
    ZipOutputStream(java::io::OutputStream* );

    virtual void putNextEntry(ZipEntry* );

    ZipOutputStream(java::io::OutputStream* , java::nio::charset::Charset* );

    void close();

    void closeEntry();

    void ensureOpen();

    void finish();

    int getExtraLen(std::vector<char>* );

    void setComment(std::string* );

    void setLevel(int );

    void setMethod(int );

    static int version(ZipEntry* );

    void write(std::vector<char>* , int , int );

    void writeByte(int );

    void writeBytes(std::vector<char>* , int , int );

    void writeCEN(XEntry* );

    void writeEND(long , long );

    void writeEXT(ZipEntry* );

    void writeExtra(std::vector<char>* );

    void writeInt(long );

    void writeLOC(XEntry* );

    void writeLong(long );

    void writeShort(int );


};//class ZipOutputStream

}//namespace java
}//namespace util
}//namespace zip
