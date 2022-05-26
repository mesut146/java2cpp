#pragma once


namespace java{
namespace util{
namespace jar{

class JarOutputStream: public java::util::zip::ZipOutputStream{
//fields
public:
    static int JAR_MAGIC;
    bool firstEntry;

//methods
public:
    JarOutputStream(java::io::OutputStream* );

    JarOutputStream(java::io::OutputStream* , Manifest* );

    static int get16(std::vector<char>* , int );

    static bool hasMagic(std::vector<char>* );

    void putNextEntry(java::util::zip::ZipEntry* );

    static void set16(std::vector<char>* , int , int );


};//class JarOutputStream

}//namespace java
}//namespace util
}//namespace jar
