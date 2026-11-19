#pragma once

#include "jre/java/lang/Object.h"

namespace java{
namespace util{

template <typename T>
class Optional {
    T value_;
    bool present_;
public:
    Optional() : value_(), present_(false) {}
    explicit Optional(T value) : value_(value), present_(true) {}

    static Optional<T>* of(T value) { return new Optional<T>(value); }
    static Optional<T>* empty() { return new Optional<T>(); }
    bool isPresent() const { return present_; }
    bool isEmpty() const { return !present_; }
    T get() { return value_; }
    T orElse(T fallback) { return present_ ? value_ : fallback; }
};

}//namespace util
}//namespace java
