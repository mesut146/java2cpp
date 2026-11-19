#pragma once

#include <cstdlib>
namespace java{ namespace util {
class Random {
public:
    Random() {}
    int nextInt() { return std::rand(); }
    int nextInt(int bound) { return bound <= 0 ? 0 : std::rand() % bound; }
    long nextLong() { return static_cast<long>(std::rand()) << 31 | std::rand(); }
    double nextDouble() { return static_cast<double>(std::rand()) / (RAND_MAX + 1.0); }
    bool nextBoolean() { return (std::rand() & 1) != 0; }
};
}}
