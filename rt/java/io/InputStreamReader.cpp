#include "InputStreamReader.h"
#include <stdexcept>

using namespace java::io;

InputStreamReader::InputStreamReader(InputStream *in) {
    this->in = in;
}

void InputStreamReader::close() {
    in->close();
}

int InputStreamReader::read() {
    return in->read();
}

int InputStreamReader::read(std::vector<wchar_t> *arr, int off, int len) {
    auto buf = new std::vector<char>(arr->size());
    int read = in->read(buf, off, len);
    arr->insert(arr->begin(), buf->begin(), buf->end());
    return read;
}

bool InputStreamReader::ready() {
    throw std::runtime_error("InputStreamReader.ready");
}
