#pragma once

//java.lang.Throwable backend, stack traces and try-with-resources guard
#include <cstdlib>
#include <cxxabi.h>
#if __has_include(<execinfo.h>)
#include <execinfo.h>
#define J2CPP_HAS_BACKTRACE 1
#endif
#include <string>
#include <typeinfo>
#include <utility>
#include <vector>
#include "j2cpp_io.h"

namespace CppHelper {

inline std::string demangleMangled(const char *mangled) {
    int status = 0;
    char *raw = abi::__cxa_demangle(mangled, nullptr, nullptr, &status);
    std::string name(raw && status == 0 ? raw : mangled);
    std::free(raw);
    return name;
}

//backtrace_symbols produces "binary(_Z...+0x..) [0x..]"; demangle the symbol
inline std::string demangleFrame(const std::string &frame) {
    std::size_t open = frame.find('(');
    if (open == std::string::npos) return frame;
    std::size_t plus = frame.find('+', open);
    std::size_t close = frame.find(')', open);
    if (plus == std::string::npos || close == std::string::npos || plus > close) return frame;
    std::string sym = frame.substr(open + 1, plus - open - 1);
    if (sym.empty()) return frame;
    std::string dem = demangleMangled(sym.c_str());
    if (dem.size() > 100) dem = dem.substr(0, 97) + "...";
    return frame.substr(0, open + 1) + dem + frame.substr(plus);
}

inline std::string demangledTypeName(const std::type_info &info) {
    std::string name = demangleMangled(info.name());
    //e::E_Boom -> e.E$Boom
    std::string out;
    for (std::size_t i = 0; i < name.size(); ++i) {
        if (name.compare(i, 2, "::") == 0) {
            out += '.';
            ++i;
        }
        else if (name[i] == '_') {
            out += '$';
        }
        else {
            out += name[i];
        }
    }
    return out;
}

class Throwable {
    std::string message;
    Throwable *cause;
    std::vector<std::string> suppressed;
public:
    explicit Throwable(std::string *m = nullptr)
        : message(m ? *m : std::string()), cause(nullptr) {}
    Throwable(std::string *m, Throwable *c)
        : message(m ? *m : std::string()), cause(c) {}
    Throwable(Throwable *other)
        : message(other ? other->message : std::string()), cause(other ? other->cause : nullptr) {}
    virtual ~Throwable() = default;

    std::string *getMessage() { return new std::string(message); }
    Throwable *getCause() { return cause; }
    Throwable *initCause(Throwable *c) { cause = c; return this; }
    void addSuppressed(Throwable *t) {
        if (t) suppressed.push_back(*t->toString());
    }

    std::string *toString() {
        return new std::string(demangledTypeName(typeid(*this)) + ": " + message);
    }
    void printStackTrace() { printStackTrace(javaErr()); }
    void printStackTrace(PrintStream *out) {
        if (!out) return;
        out->println(toString());
#ifdef J2CPP_HAS_BACKTRACE
        void *frames[32];
        int count = backtrace(frames, 32);
        char **symbols = backtrace_symbols(frames, count);
        for (int i = 1; i < count && i < 16; ++i) {
            out->println(new std::string(std::string("\tat ")
                    + (symbols ? demangleFrame(symbols[i]) : "?")));
        }
        std::free(symbols);
#endif
        for (const std::string &s : suppressed) {
            out->println(new std::string("\tSuppressed: " + s));
        }
        if (cause) {
            out->println(new std::string("Caused by: "));
            cause->printStackTrace(out);
        }
    }
};

//try-with-resources: call close() on scope exit, including during exception unwinding
template <typename F>
class ScopeGuard {
    F cleanup;
public:
    explicit ScopeGuard(F f) : cleanup(std::move(f)) {}
    ScopeGuard(const ScopeGuard &) = delete;
    ScopeGuard &operator=(const ScopeGuard &) = delete;
    ~ScopeGuard() { cleanup(); }
};

} //namespace CppHelper
