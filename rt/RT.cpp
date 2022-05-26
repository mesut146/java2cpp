#include <iostream>
#include <vector>
#include "java/io/PrintStream.h"
#include "java/io/PrintWriter.h"
#include "java/lang/System.h"

using namespace java::lang;
using namespace java::io;


class out0 : public OutputStream {
    void write(int val) override {
        std::cout << val;
    }
};


class err0 : public OutputStream {
    void write(int val) override {
        std::cerr << val;
    }
};

java::io::PrintStream *System::err() {
    auto static a = new PrintStream(new err0());
    return a;
}

java::io::PrintWriter *System::out() {
    auto static a = new PrintWriter(new out0());
    return a;
}

int System::identityHashCode(void *ptr) {
    //std::cout << "System::identityHashCode\n";
    auto p = reinterpret_cast<long>(ptr);
    return std::hash<long>{}(p);
}
