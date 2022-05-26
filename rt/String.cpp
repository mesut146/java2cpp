#include "String.h"

String::String(std::string s) {
    str = s;
}

String::String(const char *s) {
    str = s;
}

String::String(std::string *s) {
    str = *s;
}

String::String() = default;

bool String::equals(String *other) {
    return str == other->str;
}

int String::hashCode() {
    return std::hash<std::string>{}(str);
}

String *String::replace(char c1, char c2) {
    throw 5;
    return nullptr;
}


String String::operator+(String *other) {
    std::string s = str;
    s.append(other->str);
    return String(&s);
}

String String::operator+(String &other) {
    std::string s = str;
    s.append(other.str);
    return String(&s);
}

String String::operator+(const char *other) {
    std::string s = str;
    s.append(other);
    return String(&s);
}

String String::operator+(std::string *other) {
    std::string s = str;
    s.append(*other);
    return String(&s);
}

String String::operator+(std::string other) {
    std::string s = str;
    s.append(other);
    return String(&s);
}

int String::compareTo(String *other) {
    return str.compare(other->str);
}

int String::compareTo(String &other) {
    return str.compare(other.str);
}


char String::at(int pos) {
    return str[pos];
}

char String::operator[](int pos) {
    return str[pos];
}

String &String::substring(int start) {
    return *new String(str.substr(start));
}

int String::length() {
    return str.size();
}

int String::size() {
    return length();
}

String &String::substring(int start, int end) {
    return *new String(str.substr(start, end - start));
}

bool String::operator==(std::string &other) {
    return str == other;
}

bool String::operator==(String &other) {
    return str == other.str;
}

String *String::valueOf(int val) {
    return new String(std::to_string(val));
}

String *String::valueOf(long val) {
    return new String(std::to_string(val));
}

String *String::valueOf(float val) {
    return new String(std::to_string(val));
}

String *String::valueOf(double val) {
    return new String(std::to_string(val));
}

String *String::valueOf(char val) {
    return new String(std::string(&val));
}

String String::operator+(char other) {
    return operator+(valueOf(other));
}

int String::indexOf(char ch) {
    auto i = str.find(ch);
    if (i == std::string::npos) {
        return -1;
    }
    return i;
}

int String::indexOf(String &s) {
    auto i = str.find(s.str);
    if (i == std::string::npos) {
        return -1;
    }
    return i;
}

String *String::replace(String &s1, String &s2) {
    int pos = 0;
    String res;
    while (pos < str.size()) {
        int i = indexOf(s1);
        if (i == -1) {
            res = res + substring(pos);
            break;
        }
        res = res + substring(pos, i);
        res = res + s2;
        pos = i + s1.length();
    }
    return new String(res);
}

String *String::replace(String *s1, String *s2) {
    return replace(*s1, *s2);
}

String *String::ptr2() {
    return this;
}

String operator+(const char *lhs, String rhs) {
    return rhs + lhs;
}

String operator+(std::string &&lhs, String rhs) {
    return rhs + lhs;
}

String operator+(std::string &lhs, String rhs) {
    return rhs + lhs;
}
