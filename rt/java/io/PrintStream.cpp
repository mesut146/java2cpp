#include "java/lang/InterruptedException.h"
#include "PrintStream.h"
#include "IOException.h"
#include <thread>

using namespace java::io;
using namespace java::lang;

//mutex

void PrintStream::println(std::string *s) {
    print(s);
    newLine();
}

void PrintStream::print(bool b) {
    write(new std::string(b ? "true" : "false"));
}

void PrintStream::write(std::string *s) {
    write(new std::vector<char>(s->begin(), s->end()), 0, s->size());
}

PrintStream::PrintStream(OutputStream *o) : FilterOutputStream(o) {

}

PrintStream::PrintStream(bool fl, OutputStream *o) : FilterOutputStream(o) {
    charOut = new OutputStreamWriter(o);
    textOut = new BufferedWriter(charOut);
}

void PrintStream::println(bool b) {
    print(b);
    newLine();
}

void PrintStream::print(std::string *s) {
    if (s == nullptr) {
        s = new std::string("null");
    }
    write(s);
}

void PrintStream::newLine() {
    write(new std::string("\n"));
}

PrintStream::~PrintStream() = default;

void PrintStream::close() {
    if (!closing) {
        closing = true;
        try {
            textOut->close();
            out->close();
        }
        catch (IOException *x) {
            trouble = true;
        }
        textOut = nullptr;
        charOut = nullptr;
        out = nullptr;
    }

}

void PrintStream::flush() {
    try {
        ensureOpen();
        out->flush();
    }
    catch (IOException *x) {
        trouble = true;
    }
}

void PrintStream::ensureOpen() {
    if (out == nullptr)
        throw new IOException(new std::string("Stream closed"));
}


void PrintStream::write(int b) {
    try {
        ensureOpen();
        out->write(b);
        if ((b == '\n') && autoFlush)
            out->flush();

    }
    catch (InterruptedException *x) {
        std::this_thread::yield();
        //todo
        //Thread.currentThread().interrupt();
    }
    catch (IOException *x) {
        trouble = true;
    }
}

void PrintStream::write(std::vector<char> *buf, int off, int len) {
    try {

        ensureOpen();
        out->write(buf, off, len);
        if (autoFlush)
            out->flush();
    }
    catch (InterruptedException x) {
        //Thread.currentThread().interrupt();
    }
    catch (IOException x) {
        trouble = true;
    }
}

void PrintStream::println() {
    newLine();
}

PrintStream *PrintStream::append(wchar_t ch) {
    print(ch);
    return this;
}

void PrintStream::write(std::vector<wchar_t> *arr) {
    write(new std::vector<char>(arr->begin(), arr->end()), 0, arr->size());
}
