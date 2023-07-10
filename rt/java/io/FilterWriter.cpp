#include "FilterWriter.h"

using namespace java::io;

FilterWriter::FilterWriter(Writer *w) {
    this->out = w;
}

void FilterWriter::close() {
    this->out->close();
}

void FilterWriter::flush() {
    this->out->flush();
}

void FilterWriter::write(std::vector<wchar_t> *arr, int off, int len) {
    this->out->write(arr, off, len);
}

void FilterWriter::write(std::string *s) {
    this->out->write(s);
}

void FilterWriter::write(int i) {
    out->write(i);
}

void FilterWriter::write(std::string *s, int off, int len) {
    out->write(s, off, len);
}
