#pragma once


namespace java{
namespace security{

class Delegate: public MessageDigest, public sun::security::util::MessageDigestSpi2{
//fields
public:
    MessageDigestSpi* digestSpi;

//methods
public:
    Delegate(MessageDigestSpi* , std::string* );

    java::lang::Object* clone();

    std::vector<char>* engineDigest();

    int engineDigest(std::vector<char>* , int , int );

    int engineGetDigestLength();

    void engineReset();

    void engineUpdate(char );

    void engineUpdate(java::nio::ByteBuffer* );

    void engineUpdate(javax::crypto::SecretKey* );

    void engineUpdate(std::vector<char>* , int , int );


};//class Delegate

}//namespace java
}//namespace security
