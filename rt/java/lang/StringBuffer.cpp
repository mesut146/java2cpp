#include "StringBuffer.h"

using namespace java::lang;

StringBuffer::StringBuffer() : AbstractStringBuilder(16) {

}

StringBuffer::StringBuffer(int capacity) : AbstractStringBuilder(capacity) {

}

StringBuffer::~StringBuffer() = default;

StringBuffer *StringBuffer::append(std::string *s) {
    AbstractStringBuilder::append(s);
    return this;
}

StringBuffer *StringBuffer::append(std::vector<wchar_t> *arr) {
    AbstractStringBuilder::append(arr);
    return this;
}

StringBuffer *StringBuffer::append(bool b) {
    AbstractStringBuilder::append(b);
    return this;
}

StringBuffer *StringBuffer::append(int b) {
    AbstractStringBuilder::append(b);
    return this;
}

StringBuffer *StringBuffer::append(float b) {
    AbstractStringBuilder::append(b);
    return this;
}

StringBuffer *StringBuffer::append(wchar_t b) {
    AbstractStringBuilder::append(b);
    return this;
}

StringBuffer *StringBuffer::append(long b) {
    AbstractStringBuilder::append(b);
    return this;
}

StringBuffer *StringBuffer::append(double b) {
    AbstractStringBuilder::append(b);
    return this;
}

StringBuffer *StringBuffer::append(std::vector<wchar_t> *arr, int off, int len) {
    AbstractStringBuilder::append(arr, off, len);
    return this;
}

std::string *StringBuffer::toString() {
    return AbstractStringBuilder::toString();
}


int StringBuffer::length() {
    return AbstractStringBuilder::length();
}

wchar_t StringBuffer::charAt(int i) {
    return AbstractStringBuilder::charAt(i);
}

std::string *StringBuffer::substring(int from) {
    return AbstractStringBuilder::substring(from);
}

std::string *StringBuffer::substring(int from, int to) {
    return AbstractStringBuilder::substring(from, to);
}

StringBuffer *StringBuffer::delete_renamed(int from, int to) {
    AbstractStringBuilder::delete_renamed(from, to);
    return this;
}

int StringBuffer::indexOf(std::string *s) {
    return AbstractStringBuilder::indexOf(s);
}

int StringBuffer::indexOf(std::string *s, int from) {
    return AbstractStringBuilder::indexOf(s, from);
}