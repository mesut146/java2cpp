#pragma once


namespace java{
namespace util{
namespace zip{

class InflaterInputStream: public java::io::FilterInputStream{
//methods
public:
    virtual int available();

    virtual void close();

    virtual void fill();


};//class InflaterInputStream

}//namespace java
}//namespace util
}//namespace zip
