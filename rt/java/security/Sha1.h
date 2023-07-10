#pragma once

#include "MessageDigest.h"
#include "lib/crypto/cryptlib.h"
#include "lib/crypto/sha.h"

using namespace java::security;
using namespace CryptoPP;

namespace java {
    namespace security {
        class Sha1 : public MessageDigest {
            SHA1 sha1;
        public:
            Sha1();

            virtual ~Sha1();

            int engineDigest(std::vector<char> *buf, int offset, int len) override;

            std::vector<char> *engineDigest() override;

            void engineUpdate(std::vector<char> *arr, int offset, int len) override;
        };

    }
}