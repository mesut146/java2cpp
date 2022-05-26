#pragma once


namespace java{
namespace util{
namespace jar{

class Manifest: public java::lang::Cloneable{
//fields
public:
    Attributes* attr;
    java::util::Map<std::string*, Attributes*>* entries;
    JarVerifier* jv;

//methods
public:
    Manifest();

    Manifest(java::io::InputStream* );

    Manifest(Manifest* );

    Manifest(JarVerifier* , java::io::InputStream* );

    void clear();

    java::lang::Object* clone();

    bool equals(java::lang::Object* );

    Attributes* getAttributes(std::string* );

    java::util::Map<std::string*, Attributes*>* getEntries();

    Attributes* getMainAttributes();

    Attributes* getTrustedAttributes(std::string* );

    int hashCode();

    static void make72Safe(java::lang::StringBuffer* );

    std::string* parseName(std::vector<char>* , int );

    void read(java::io::InputStream* );

    int toLower(int );

    void write(java::io::OutputStream* );


};//class Manifest

}//namespace java
}//namespace util
}//namespace jar
