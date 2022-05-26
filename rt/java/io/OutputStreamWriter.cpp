#include <string>
#include "OutputStreamWriter.h"

using namespace java::io;

OutputStreamWriter::OutputStreamWriter(OutputStream *os) {
    this->os = os;
}

void OutputStreamWriter::close() {
    os->close();
}

void OutputStreamWriter::flush() {
    os->flush();
}

void OutputStreamWriter::write(int c) {
    os->write(c);
}

void OutputStreamWriter::write(std::vector<wchar_t> *arr, int off, int len) {

}

void OutputStreamWriter::write(std::string *s, int off, int len) {
    for (int i = 0; i < len; i++) {
        os->write(s->at(i + off));
    }
}


