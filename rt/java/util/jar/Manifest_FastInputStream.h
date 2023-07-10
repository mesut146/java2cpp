#pragma once


namespace java{
namespace util{
namespace jar{

class FastInputStream: public java::io::FilterInputStream{
//fields
public:
    std::vector<char>* buf;
    int count;
    int pos;

//methods
public:
    FastInputStream(java::io::InputStream* );

    FastInputStream(java::io::InputStream* , int );

    int available();

    void close();

    void fill();

    char peek();

    int read();

    int read(std::vector<char>* , int , int );

    int readLine(std::vector<char>* );

    int readLine(std::vector<char>* , int , int );

    long skip(long );


};//class FastInputStream

}//namespace java
}//namespace util
}//namespace jar
