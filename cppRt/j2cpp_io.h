#pragma once

//console and java.io stream helpers
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include "j2cpp_str.h"

namespace CppHelper {

//java.lang.System.out / System.err
class PrintStream {
    std::ostream &stream;
public:
    explicit PrintStream(std::ostream &s) : stream(s) {}

    template <typename T> PrintStream *print(T value) {
        stream << detail::toString(value);
        return this;
    }
    template <typename T> PrintStream *println(T value) {
        stream << detail::toString(value) << '\n';
        return this;
    }
    PrintStream *println() { stream << '\n'; return this; }
    void flush() { stream.flush(); }

    template <typename... Args>
    PrintStream *printf(std::string *fmt, Args... args) {
        stream << *CppHelper::format(fmt, args...);
        return this;
    }
    template <typename... Args>
    PrintStream *format(std::string *fmt, Args... args) {
        stream << *CppHelper::format(fmt, args...);
        return this;
    }
};

inline PrintStream *javaOut() {
    static PrintStream out(std::cout);
    return &out;
}
inline PrintStream *javaErr() {
    static PrintStream err(std::cerr);
    return &err;
}

//java.io.FileReader / BufferedReader / Scanner / PrintWriter
class FileReader {
    std::ifstream stream;
public:
    explicit FileReader(std::string *path) {
        if (path) stream.open(*path);
    }
    std::istream &get() { return stream; }
    void close() { if (stream.is_open()) stream.close(); }
};

class BufferedReader {
    FileReader *reader;
public:
    explicit BufferedReader(FileReader *r) : reader(r) {}
    ~BufferedReader() { delete reader; }
    std::string *readLine() {
        if (!reader) return nullptr;
        std::string line;
        if (!std::getline(reader->get(), line)) return nullptr;
        if (!line.empty() && line.back() == '\r') line.pop_back();
        return new std::string(line);
    }
    void close() { delete reader; reader = nullptr; }
};

class Scanner {
    std::istream *input;
    bool owned;
public:
    explicit Scanner(std::string *s) : input(new std::istringstream(s ? *s : std::string())), owned(true) {}
    explicit Scanner(FileReader *r) : input(r ? &r->get() : nullptr), owned(false) {}
    ~Scanner() { if (owned) delete input; }
    bool hasNext() {
        if (!input) return false;
        return input->good() && input->peek() != std::char_traits<char>::eof();
    }
    std::string *next() {
        std::string s;
        if (input) *input >> s;
        return new std::string(s);
    }
    int nextInt() {
        int v = 0;
        if (input) *input >> v;
        return v;
    }
    std::string *nextLine() {
        std::string s;
        if (input) std::getline(*input, s);
        if (!s.empty() && s.back() == '\r') s.pop_back();
        return new std::string(s);
    }
    void close() { if (owned) { delete input; input = nullptr; owned = false; } }
};

class PrintWriter {
    std::ostream *output;
    bool owned;
public:
    explicit PrintWriter(std::string *path) : output(new std::ofstream(path ? *path : std::string())), owned(true) {}
    ~PrintWriter() { if (owned) delete output; }
    template <typename T> void print(T value) {
        if (output) *output << detail::toString(value);
    }
    template <typename T> void println(T value) {
        if (output) *output << detail::toString(value) << '\n';
    }
    void println() { if (output) *output << '\n'; }
    void flush() { if (output) output->flush(); }
    void close() { if (owned) { delete output; output = nullptr; owned = false; } }
};

} //namespace CppHelper
