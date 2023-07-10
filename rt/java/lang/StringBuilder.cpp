#include <vector>
#include <string>
#include "java/lang/StringBuilder.h"

using namespace java::lang;

StringBuilder::StringBuilder() : StringBuilder(16) {}

StringBuilder::StringBuilder(int s) : AbstractStringBuilder(s) {}

StringBuilder::StringBuilder(std::string *s) : AbstractStringBuilder(s->size() + 16) {
    append(s);
    //AbstractStringBuilder::append(s);
}

StringBuilder *StringBuilder::append(std::string *s) {
    AbstractStringBuilder::append(s);
    return this;
}

StringBuilder *StringBuilder::append(std::vector<wchar_t> *arr) {
    AbstractStringBuilder::append(arr);
    return this;
}

StringBuilder *StringBuilder::append(bool b) {
    AbstractStringBuilder::append(b);
    return this;
}

StringBuilder *StringBuilder::append(int b) {
    AbstractStringBuilder::append(b);
    return this;
}

StringBuilder *StringBuilder::append(float b) {
    AbstractStringBuilder::append(b);
    return this;
}

StringBuilder *StringBuilder::append(wchar_t b) {
    AbstractStringBuilder::append(b);
    return this;
}

StringBuilder *StringBuilder::append(long b) {
    AbstractStringBuilder::append(b);
    return this;
}

StringBuilder *StringBuilder::append(double b) {
    AbstractStringBuilder::append(b);
    return this;
}

StringBuilder *StringBuilder::append(std::vector<wchar_t> *arr, int off, int len) {
    AbstractStringBuilder::append(arr, off, len);
    return this;
}

std::string *StringBuilder::toString() {
    return AbstractStringBuilder::toString();
}
