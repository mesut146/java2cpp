#pragma once

#include <string>

class String {
public:
    std::string str;

    String();

    explicit String(std::string *s);

    explicit String(const char *s);

    explicit String(std::string s);

    std::string * ptr() const {
        return (std::string *) &str;
    }

    int hashCode();

    bool equals(String *other);

    int compareTo(String *other);

    int compareTo(String &other);

    char at(int pos);

    char operator[](int pos);

    String *replace(char c1, char c2);

    String *replace(String &s1, String &s2);

    String *replace(String *s1, String *s2);

    String operator+(String *other);

    String operator+(String &other);

    String operator+(const char *other);

    String operator+(std::string *other);

    String operator+(std::string other);

    String operator+(char other);

    bool operator==(std::string &other);

    bool operator==(String &other);

    String &substring(int start);

    String &substring(int start, int end);

    int size();

    int length();

    static String *valueOf(long val);

    static String *valueOf(int val);

    static String *valueOf(char val);

    static String *valueOf(float val);

    static String *valueOf(double val);

    int indexOf(char ch);

    int indexOf(String &s);

    String *ptr2();
};

String operator+(const char *lhs, String rhs);

String operator+(std::string &&lhs, String rhs);

String operator+(std::string &lhs, String rhs);