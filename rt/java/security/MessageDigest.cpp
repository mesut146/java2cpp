#include "java/lang/Exception.h"
#include "java/lang/IllegalArgumentException.h"
#include "MessageDigest.h"
#include "Sha1.h"

using namespace java::security;
using namespace java::lang;

std::string *MessageDigest::getAlgorithm() {
    return algorithm;
}

MessageDigest::MessageDigest(std::string *s) {
    this->algorithm = s;
}

MessageDigest *MessageDigest::getInstance(std::string *algorithm) {
    if (*algorithm == "SHA-1") {
        return new Sha1();
    } else {
        throw java::lang::Exception("no such algorithm:" + *algorithm);
    }
}

void MessageDigest::update(std::vector<char> *arr) {
    update(arr, 0, arr->size());
}

void MessageDigest::update(std::vector<char> *input, int offset, int len) {
    if (input == nullptr) {
        throw IllegalArgumentException(new std::string("No input buffer given"));
    }
    if (input->size() - offset < len) {
        throw IllegalArgumentException(new std::string("Input buffer too short"));
    }
    engineUpdate(input, offset, len);
    state = IN_PROGRESS;
}

std::vector<char> *MessageDigest::digest() {
    /* Resetting is the responsibility of implementors. */
    auto result = engineDigest();
    state = INITIAL;
    return result;
}

int MessageDigest::digest(std::vector<char> *buf, int offset, int len) {
    if (buf == nullptr) {
        throw IllegalArgumentException("No output buffer given");
    }
    if (buf->size() - offset < len) {
        throw IllegalArgumentException
                ("Output buffer too small for specified offset and length");
    }
    int numBytes = engineDigest(buf, offset, len);
    state = INITIAL;
    return numBytes;
}

