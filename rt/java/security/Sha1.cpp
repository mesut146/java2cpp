#include "Sha1.h"
#include "CppHelper.h"

Sha1::Sha1() : MessageDigest(nullptr) {

}

std::vector<char> *Sha1::engineDigest() {
    std::vector<char> v;
    sha1.Final((byte *) v.data());
    return new std::vector<char>(v);
}

Sha1::~Sha1() = default;

int Sha1::engineDigest(std::vector<char> *buf, int offset, int len) {
    std::vector<char> v;
    sha1.CalculateDigest((byte *) v.data(), (byte *) buf->data(), len);
    return len;
}

void Sha1::engineUpdate(std::vector<char> *arr, int offset, int len) {
    byte *buf = new byte[len - offset];
    for (int i = offset; i < len; i++) {
        buf[i - offset] = arr->at(i);
    }
    sha1.Update(buf, len - offset);
}
