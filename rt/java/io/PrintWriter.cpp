#include <string>
#include <vector>
#include "OutputStream.h"
#include "OutputStreamWriter.h"
#include "PrintWriter.h"

using namespace java::io;

PrintWriter::PrintWriter(Writer *w) : PrintWriter(w, false) {
}

PrintWriter::PrintWriter(Writer *w, bool fl) {
    this->out = w;
    this->autoFlush = fl;
}

PrintWriter::PrintWriter(OutputStream *os, bool fl) {
    this->out = new OutputStreamWriter(os);
    this->autoFlush = fl;
}

PrintWriter::PrintWriter(OutputStream *os) : PrintWriter(os, false) {
}


void PrintWriter::close() {
    out->close();
}

void PrintWriter::flush() {
    out->flush();
}

void PrintWriter::newLine() {
    out->write(lineSeparator);
    if (autoFlush) {
        flush();
    }
}

void PrintWriter::println() {
    newLine();
}

PrintWriter *PrintWriter::append(wchar_t c) {
    out->write(c);
    return this;
}

void PrintWriter::println(bool b) {
    print(b);
    newLine();
}

void PrintWriter::println(int b) {
    print(b);
    newLine();
}

void PrintWriter::println(wchar_t b) {
    print(b);
    newLine();
}

void PrintWriter::println(long b) {
    print(b);
    newLine();
}

void PrintWriter::println(float b) {
    print(b);
    newLine();
}

void PrintWriter::println(double b) {
    print(b);
    newLine();
}

void PrintWriter::println(std::string *b) {
    print(b);
    newLine();
}

void PrintWriter::println(std::vector<wchar_t> *b) {
    print(b);
    newLine();
}

void PrintWriter::print(bool b) {
    out->write(new std::string(b ? "true" : "false"));
}

void PrintWriter::print(std::string *s) {
    out->write(s);
}

void PrintWriter::print(wchar_t c) {
    out->write(c);
}

void PrintWriter::print(int c) {
    out->write(new std::string(std::to_string(c)));
}

void PrintWriter::print(long c) {
    out->write(new std::string(std::to_string(c)));
}

void PrintWriter::print(float c) {
    out->write(new std::string(std::to_string(c)));
}

void PrintWriter::print(double c) {
    out->write(new std::string(std::to_string(c)));
}

void PrintWriter::print(std::vector<wchar_t> *arr) {
    out->write(arr);
}

void PrintWriter::write(int c) {
    out->write(c);
}

void PrintWriter::write(std::vector<wchar_t> *arr) {
    write(arr, 0, arr->size());
}

void PrintWriter::write(std::string *s) {
    out->write(s);
}

void PrintWriter::write(std::string *s, int off, int len) {
    out->write(s, off, len);
}

void PrintWriter::write(std::vector<wchar_t> *arr, int off, int len) {
    out->write(arr, off, len);
}
