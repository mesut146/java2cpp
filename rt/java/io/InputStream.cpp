#include <vector>
#include <string>
#include <java/lang/NullPointerException.h>
#include <java/lang/IndexOutOfBoundsException.h>
#include "InputStream.h"
#include "IOException.h"

using namespace java::io;

static constexpr int MAX_SKIP_BUFFER_SIZE = 2048;

InputStream::InputStream() = default;

int InputStream::read(std::vector<char> *arr) {
    return read(arr, 0, arr->size());
}

int InputStream::read(std::vector<char> *b, int off, int len) {
    if (b == nullptr) {
        throw new java::lang::NullPointerException();
    } else if (off < 0 || len < 0 || len > b->size() - off) {
        throw new java::lang::IndexOutOfBoundsException();
    } else if (len == 0) {
        return 0;
    }
    int c = read();
    if (c == -1) {
        return -1;
    }
    (*b)[off] = (char) c;
    int i = 1;
    try {
        for (; i < len; i++) {
            c = read();
            if (c == -1) {
                break;
            }
            (*b)[off + i] = (char) c;
        }
    } catch (IOException *ee) {
    }
    return i;
}

long InputStream::skip(long n) {
    long remaining = n;
    int nr;
    if (n <= 0) {
        return 0;
    }
    int size = (int) std::min(MAX_SKIP_BUFFER_SIZE, (int) remaining);
    auto *skipBuffer = new std::vector<char>[size];
    while (remaining > 0) {
        nr = read(skipBuffer, 0, (int) std::min(size, (int) remaining));
        if (nr < 0) {
            break;
        }
        remaining -= nr;
    }

    return n - remaining;
}

void InputStream::close() {

}

void InputStream::reset() {
    throw new IOException(new std::string("mark/reset not supported"));
}

int InputStream::available() {
    return 0;
}
