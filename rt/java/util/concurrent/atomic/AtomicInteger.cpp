#include "AtomicInteger.h"

using namespace java::util::concurrent::atomic;

AtomicInteger::AtomicInteger() {
    this->value = 0;
}


AtomicInteger::AtomicInteger(int x) {
    this->value = x;
}

int AtomicInteger::get() {
    return value;
}

int AtomicInteger::incrementAndGet() {
    return ++value;
}

