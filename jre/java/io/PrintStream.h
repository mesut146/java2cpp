#pragma once

#include <iostream>
#include <sstream>
#include <string>
#include <vector>
#include "jre/java/lang/String.h"

namespace java{
namespace io{

class PrintStream {
    std::ostream& out_;
public:
    explicit PrintStream(std::ostream& out) : out_(out) {}

    void print(bool v) { out_ << (v ? "true" : "false"); }
    void print(wchar_t v) { out_ << static_cast<char>(v); }
    void print(char v) { out_ << v; }
    void print(int v) { out_ << v; }
    void print(long v) { out_ << v; }
    void print(float v) { out_ << v; }
    void print(double v) { out_ << v; }
    void print(std::vector<wchar_t>* v) {
        if (!v) return;
        for (wchar_t c : *v) out_ << static_cast<char>(c);
    }
    void print(java::lang::String* v) { out_ << (v ? *v->utf8() : std::string("null")); }
    void print(java::lang::Object* v) {
        if (!v) out_ << "null";
        else print(v->toString());
    }
    void println() { out_ << '\n'; }
    void println(bool v) { print(v); println(); }
    void println(wchar_t v) { print(v); println(); }
    void println(char v) { print(v); println(); }
    void println(int v) { print(v); println(); }
    void println(long v) { print(v); println(); }
    void println(float v) { print(v); println(); }
    void println(double v) { print(v); println(); }
    void println(std::vector<wchar_t>* v) { print(v); println(); }
    void println(java::lang::String* v) { print(v); println(); }
    void println(java::lang::Object* v) { print(v); println(); }
};

}//namespace io
}//namespace java
