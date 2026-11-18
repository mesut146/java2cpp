#pragma once

//java.util.Random / Math.random() / java.util.UUID backend
#include <random>
#include <string>

namespace CppHelper {

inline std::mt19937_64 &rng() {
    static std::mt19937_64 engine(std::random_device{}());
    return engine;
}
inline double nextDouble() {
    return std::generate_canonical<double, 53>(rng());
}
inline int nextInt() { return static_cast<int>(rng()()); }
inline int nextInt(int bound) {
    if (bound <= 0) return 0;
    return static_cast<int>(rng()() % static_cast<unsigned long long>(bound));
}
inline long long nextLong() { return static_cast<long long>(rng()()); }
inline bool nextBoolean() { return (rng()() & 1) != 0; }
inline double nextGaussian() { return std::normal_distribution<double>(0.0, 1.0)(rng()); }
class Random {
public:
    int nextInt() { return CppHelper::nextInt(); }
    int nextInt(int bound) { return CppHelper::nextInt(bound); }
    long long nextLong() { return CppHelper::nextLong(); }
    double nextDouble() { return CppHelper::nextDouble(); }
    bool nextBoolean() { return CppHelper::nextBoolean(); }
};
inline std::string *randomUUID() {
    static const char *hex = "0123456789abcdef";
    std::string s = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";
    for (char &c : s) {
        if (c == 'x') c = hex[nextInt(16)];
        else if (c == 'y') c = hex[8 + nextInt(4)];
    }
    return new std::string(s);
}

template <typename T> int signum(T v) { return (v > 0) - (v < 0); }

} //namespace CppHelper
