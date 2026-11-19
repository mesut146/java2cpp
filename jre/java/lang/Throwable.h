#pragma once

#include <iostream>
#include <string>
#include <vector>
#include "jre/java/lang/String.h"
#include "jre/java/io/PrintStream.h"

namespace java{
namespace lang{

class Throwable : public Object {
    String* message_;
    Throwable* cause_;
    std::vector<Throwable*> suppressed_;
public:
    Throwable() : message_(nullptr), cause_(nullptr) {}
    explicit Throwable(String* message) : message_(message), cause_(nullptr) {}
    Throwable(String* message, Throwable* cause) : message_(message), cause_(cause) {}
    explicit Throwable(Throwable* cause) : message_(nullptr), cause_(cause) {}

    String* getMessage() { return message_; }
    String* getLocalizedMessage() { return getMessage(); }
    Throwable* getCause() { return cause_; }
    Throwable* initCause(Throwable* cause) { cause_ = cause; return this; }
    void addSuppressed(Throwable* t) { if (t) suppressed_.push_back(t); }
    std::vector<Throwable*>* getSuppressed() { return &suppressed_; }
    Throwable* fillInStackTrace() { return this; }

    String* toString() override {
        std::string out = "java.lang.Throwable";
        if (message_) out += ": " + *message_->utf8();
        return new String(new std::string(out));
    }
    void printStackTrace() {
        java::io::PrintStream err(std::cerr);
        printStackTrace(err);
    }
    void printStackTrace(java::io::PrintStream& out) {
        out.println(toString());
        for (Throwable* t : suppressed_) out.println(t->toString());
        if (cause_) out.println(cause_->toString());
    }
};

}//namespace lang
}//namespace java
