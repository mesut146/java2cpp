#pragma once

#include <string>
#include <vector>

namespace java {
    namespace security {

        //MessageDigestSpi
        class MessageDigest {
//fields
        public:
            static constexpr int INITIAL = 0;
            static constexpr int IN_PROGRESS = 1;
            std::string *algorithm;
            static bool skipDebug;
            int state = INITIAL;

//methods
        public:
            explicit MessageDigest(std::string *s);

            //virtual java::lang::Object *clone();

            std::vector<char> *digest();

            //std::vector<char> *digest(std::vector<char> *arr);

            int digest(std::vector<char> *arr, int off, int len);

            std::string *getAlgorithm();

            //int getDigestLength();

            static MessageDigest *getInstance(std::string *);

            //static MessageDigest *getInstance(std::string *, std::string *);

            //static bool isEqual(std::vector<char> *, std::vector<char> *);

            //void reset();

            //std::string *toString();

            //void update(char);

            void update(std::vector<char> *arr);

            //void update(java::nio::ByteBuffer *);

            void update(std::vector<char> *arr, int off, int len);


            virtual void engineUpdate(std::vector<char> *arr, int offset, int len) = 0;

            virtual std::vector<char> *engineDigest() = 0;

            virtual int engineDigest(std::vector<char> *buf, int offset, int len) = 0;
        };//class MessageDigest

    }//namespace java
}//namespace security
