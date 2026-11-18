#pragma once

//Java try/finally control flow helpers
#include <optional>
#include <utility>

namespace j2cpp {

//Execute try and finally with Java semantics: finally always runs, and a value
//returned from finally overrides the try/catch result or any pending exception.
//An exception thrown from finally replaces any pending exception.
//
//The generator passes the try/catch part and the finally part as lambdas that
//return std::optional<R> (with R the method's return type). A Java `return X;`
//becomes `return X;` inside the lambda (implicitly wrapped in the optional);
//falling off the end becomes `return std::nullopt;`. An empty optional from the
//try part means the try block completed normally without returning, so execution
//continues after the try/finally.
template <typename TryCode, typename FinallyCode>
auto try_finally_value(TryCode&& tryCode, FinallyCode&& finallyCode) -> decltype(tryCode()) {
    decltype(tryCode()) result;
    try {
        result = tryCode();
    }
    catch (...) {
        auto fin = finallyCode();
        if (fin.has_value()) return fin;
        throw;
    }
    auto fin = finallyCode();
    if (fin.has_value()) return fin;
    return result;
}

//void version: the try lambda returns true if it executed a Java `return;`.
template <typename TryCode, typename FinallyCode>
bool try_finally_void(TryCode&& tryCode, FinallyCode&& finallyCode) {
    bool returned = false;
    try {
        returned = tryCode();
    }
    catch (...) {
        finallyCode();
        throw;
    }
    finallyCode();
    return returned;
}

} //namespace j2cpp
