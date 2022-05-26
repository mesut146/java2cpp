#include <java/lang/IndexOutOfBoundsException.h>
#include "java/lang/StringBuffer.h"
#include "java/lang/IllegalArgumentException.h"
#include "BufferedReader.h"
#include "IOException.h"
#include "CppHelper.h"

using namespace java::io;
using namespace java::lang;

BufferedReader::BufferedReader(Reader *in) : BufferedReader(in, defaultCharBufferSize) {}

BufferedReader::BufferedReader(Reader *in, int sz) {
    //super(in);
    if (sz <= 0)
        throw new IllegalArgumentException(new std::string("Buffer size <= 0"));
    this->in = in;
    cb = new std::vector<wchar_t>(sz);
    nextChar = nChars = 0;
}

std::string *BufferedReader::readLine() {
    return readLine(false);
}

std::string *BufferedReader::readLine(bool ignoreLF) {
    StringBuffer *s = nullptr;
    int startChar;

    ensureOpen();
    bool omitLF = ignoreLF || skipLF;

    for (;;) {

        if (nextChar >= nChars)
            fill();
        if (nextChar >= nChars) { /* EOF */
            if (s != nullptr && s->length() > 0)
                return s->toString();
            else
                return nullptr;
        }
        bool eol = false;
        char c = 0;
        int i;

        /* Skip a leftover '\n', if necessary */
        if (omitLF && ((*cb)[nextChar] == '\n'))
            nextChar++;
        skipLF = false;
        omitLF = false;

        for (i = nextChar; i < nChars; i++) {
            c = (*cb)[i];
            if ((c == '\n') || (c == '\r')) {
                eol = true;
                break;
            }
        }

        startChar = nextChar;
        nextChar = i;

        if (eol) {
            std::string *str;
            if (s == nullptr) {
                str = makeString(cb, startChar, i - startChar);
            } else {
                s->append(cb, startChar, i - startChar);
                str = s->toString();
            }
            nextChar++;
            if (c == '\r') {
                skipLF = true;
            }
            return str;
        }

        if (s == nullptr)
            s = new StringBuffer(defaultExpectedLineLength);
        s->append(cb, startChar, i - startChar);
    }
}

void BufferedReader::ensureOpen() {
    if (in == nullptr)
        throw new IOException(new std::string("Stream closed"));
}

void BufferedReader::fill() {
    int dst;
    if (markedChar <= UNMARKED) {
        /* No mark */
        dst = 0;
    } else {
        /* Marked */
        int delta = nextChar - markedChar;
        if (delta >= readAheadLimit) {
            /* Gone past read-ahead limit: Invalidate mark */
            markedChar = INVALIDATED;
            readAheadLimit = 0;
            dst = 0;
        } else {
            if (readAheadLimit <= cb->size()) {
                /* Shuffle in the current buffer */
                arraycopy(cb, markedChar, cb, 0, delta);
                markedChar = 0;
                dst = delta;
            } else {
                /* Reallocate buffer to accommodate read-ahead limit */
                auto ncb = new std::vector<wchar_t>(readAheadLimit);
                arraycopy(cb, markedChar, ncb, 0, delta);
                cb = ncb;
                markedChar = 0;
                dst = delta;
            }
            nextChar = nChars = delta;
        }
    }

    int n;
    do {
        n = in->read(cb, dst, cb->size() - dst);
    } while (n == 0);
    if (n > 0) {
        nChars = dst + n;
        nextChar = dst;
    }
}

int BufferedReader::read() {
    ensureOpen();
    for (;;) {
        if (nextChar >= nChars) {
            fill();
            if (nextChar >= nChars)
                return -1;
        }
        if (skipLF) {
            skipLF = false;
            if ((*cb)[nextChar] == '\n') {
                nextChar++;
                continue;
            }
        }
        return (*cb)[nextChar++];
    }
}

bool BufferedReader::markSupported() {
    return true;
}

BufferedReader::~BufferedReader() {

}

void BufferedReader::close() {
    in->close();
}

void BufferedReader::reset() {
    ensureOpen();
    if (markedChar < 0)
        throw new IOException(new std::string((markedChar == INVALIDATED)
                                              ? "Mark invalid"
                                              : "Stream not marked"));
    nextChar = markedChar;
    skipLF = markedSkipLF;
}

void BufferedReader::mark(int readAheadLimit) {
    if (readAheadLimit < 0) {
        throw new IllegalArgumentException(new std::string("Read-ahead limit < 0"));
    }
    ensureOpen();
    this->readAheadLimit = readAheadLimit;
    markedChar = nextChar;
    markedSkipLF = skipLF;

}

long BufferedReader::skip(long n) {
    if (n < 0L) {
        throw new IllegalArgumentException(new std::string("skip value is negative"));
    }
    ensureOpen();
    long r = n;
    while (r > 0) {
        if (nextChar >= nChars)
            fill();
        if (nextChar >= nChars) /* EOF */
            break;
        if (skipLF) {
            skipLF = false;
            if ((*cb)[nextChar] == '\n') {
                nextChar++;
            }
        }
        long d = nChars - nextChar;
        if (r <= d) {
            nextChar += r;
            r = 0;
            break;
        } else {
            r -= d;
            nextChar = nChars;
        }
    }
    return n - r;
}

bool BufferedReader::ready() {
    ensureOpen();

    /*
     * If newline needs to be skipped and the next char to be read
     * is a newline character, then just skip it right away.
     */
    if (skipLF) {
        /* Note that in.ready() will return true if and only if the next
         * read on the stream will not block.
         */
        if (nextChar >= nChars && in->ready()) {
            fill();
        }
        if (nextChar < nChars) {
            if ((*cb)[nextChar] == '\n')
                nextChar++;
            skipLF = false;
        }
    }
    return (nextChar < nChars) || in->ready();
}

int BufferedReader::read(std::vector<wchar_t> *cbuf, int off, int len) {
    ensureOpen();
    if ((off < 0) || (off > cbuf->size()) || (len < 0) ||
        ((off + len) > cbuf->size()) || ((off + len) < 0)) {
        throw new IndexOutOfBoundsException();
    } else if (len == 0) {
        return 0;
    }

    int n = read1(cbuf, off, len);
    if (n <= 0) return n;
    while ((n < len) && in->ready()) {
        int n1 = read1(cbuf, off + n, len - n);
        if (n1 <= 0) break;
        n += n1;
    }
    return n;
}

int BufferedReader::read1(std::vector<wchar_t> *cbuf, int off, int len) {
    if (nextChar >= nChars) {
        /* If the requested length is at least as large as the buffer, and
           if there is no mark/reset activity, and if line feeds are not
           being skipped, do not bother to copy the characters into the
           local buffer.  In this way buffered streams will cascade
           harmlessly. */
        if (len >= cb->size() && markedChar <= UNMARKED && !skipLF) {
            return in->read(cbuf, off, len);
        }
        fill();
    }
    if (nextChar >= nChars) return -1;
    if (skipLF) {
        skipLF = false;
        if ((*cb)[nextChar] == '\n') {
            nextChar++;
            if (nextChar >= nChars)
                fill();
            if (nextChar >= nChars)
                return -1;
        }
    }
    int n = std::min(len, nChars - nextChar);
    arraycopy(cb, nextChar, cbuf, off, n);
    nextChar += n;
    return n;
}