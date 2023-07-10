#include <vector>
#include <string>
#include "CppHelper.h"
#include "AbstractStringBuilder.h"
#include "StringIndexOutOfBoundsException.h"
#include "Integer.h"
#include "Long.h"

using namespace java::lang;

AbstractStringBuilder::AbstractStringBuilder(int size) {
    value = new std::vector<wchar_t>(size);
    count = 0;
}

AbstractStringBuilder::AbstractStringBuilder() : AbstractStringBuilder(16) {
}

int AbstractStringBuilder::length() {
    return count;
}

wchar_t AbstractStringBuilder::charAt(int pos) {
    return value->at(pos);
}

AbstractStringBuilder *AbstractStringBuilder::append(float f) {
    auto a = std::to_string(f);
    append(&a);
    return this;
}

AbstractStringBuilder *AbstractStringBuilder::append(wchar_t val) {
    ensureCapacityInternal(count + 1);
    (*value)[count++] = val;
    return this;
}

AbstractStringBuilder *AbstractStringBuilder::append(std::string *str) {
    if (str == nullptr) str = new std::string("null");
    int len = str->length();
    ensureCapacityInternal(count + len);
    CppHelper::getChars(str, 0, len, value, count);
    count += len;
    return this;
}

AbstractStringBuilder *AbstractStringBuilder::append(int i) {
    if (i == INT32_MIN) {
        append("-2147483648");
        return this;
    }
    int appendedLength = (i < 0) ? Integer::stringSize(-i) + 1
                                 : Integer::stringSize(i);
    int spaceNeeded = count + appendedLength;
    ensureCapacityInternal(spaceNeeded);
    Integer::getChars(i, spaceNeeded, value);
    count = spaceNeeded;
    return this;
}

AbstractStringBuilder *AbstractStringBuilder::delete_renamed(int start, int end) {
    if (start < 0)
        throw StringIndexOutOfBoundsException(start);
    if (end > count)
        end = count;
    if (start > end)
        throw StringIndexOutOfBoundsException();
    int len = end - start;
    if (len > 0) {
        arraycopy(value, start + len, value, start, count - end);
        count -= len;
    }
    return this;
}

AbstractStringBuilder *AbstractStringBuilder::append(bool b) {
    append(new std::string(b ? "true" : "false"));
    return this;
}

AbstractStringBuilder *AbstractStringBuilder::append(std::vector<wchar_t> *arr) {
    append(arr, 0, (int)arr->size());
    return this;
}

AbstractStringBuilder *AbstractStringBuilder::append(long l) {
    if (l == INT64_MIN) {
        append("-9223372036854775808");
        return this;
    }
    int appendedLength = (l < 0) ? Long::stringSize(-l) + 1
                                 : Long::stringSize(l);
    int spaceNeeded = count + appendedLength;
    ensureCapacityInternal(spaceNeeded);
    Long::getChars(l, spaceNeeded, value);
    count = spaceNeeded;
    return this;
}

AbstractStringBuilder *AbstractStringBuilder::append(double d) {
    auto tmp = std::to_string(d);
    return append(&tmp);
}

AbstractStringBuilder *AbstractStringBuilder::append(std::vector<wchar_t> *str, int offset, int len) {
    if (len > 0)                // let arraycopy report AIOOBE for len < 0
        ensureCapacityInternal(count + len);
    arraycopy(str, offset, value, count, len);
    count += len;
    return this;
}

int AbstractStringBuilder::indexOf(std::string *s) {
    return indexOf(s, 0);
}

int AbstractStringBuilder::indexOf(std::string *s, int off) {
    auto t = toString();
    auto it = t->find(*s, off);
    if (it == std::string::npos) {
        return -1;
    }
    return it;
}

void AbstractStringBuilder::ensureCapacityInternal(int minimumCapacity) {
    if ((minimumCapacity - (int)value->size()) > 0)
        expandCapacity(minimumCapacity);
}

void AbstractStringBuilder::expandCapacity(int minimumCapacity) {
    int newCapacity = (int)value->size() * 2 + 2;
    if (newCapacity - minimumCapacity < 0)
        newCapacity = minimumCapacity;
    if (newCapacity < 0) {
        if (minimumCapacity < 0) // overflow
            throw Exception(new std::string("out of memory"));
        newCapacity = INT32_MAX;
    }
    value = copyOfRange(value, 0, newCapacity);
    //value = Arrays.copyOf(value, newCapacity);
}

std::string *AbstractStringBuilder::substring(int start) {
    return substring(start, count);
}

std::string *AbstractStringBuilder::substring(int start, int end) {
    if (start < 0)
        throw StringIndexOutOfBoundsException(start);
    if (end > count)
        throw StringIndexOutOfBoundsException(end);
    if (start > end)
        throw StringIndexOutOfBoundsException(end - start);
    return makeString(value, start, end - start);
}


std::string *AbstractStringBuilder::toString() {
    return makeString(value, 0, count);
}

int AbstractStringBuilder::capacity() {
    return value->size();
}

int AbstractStringBuilder::lastIndexOf(std::string *s) {
    return lastIndexOf(s, count);
}

int AbstractStringBuilder::lastIndexOf(std::string *s, int off) {
    auto a = toString()->rfind(*s, off);
    if (a == std::string::npos) return -1;
    return (int)a;
}

void AbstractStringBuilder::setLength(int newLength) {
    if (newLength < 0)
        throw StringIndexOutOfBoundsException(newLength);
    ensureCapacityInternal(newLength);

    if (count < newLength) {
        for (; count < newLength; count++)
            (*value)[count] = '\0';
    } else {
        count = newLength;
    }
}
