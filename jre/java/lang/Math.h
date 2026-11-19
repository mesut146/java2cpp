#pragma once

#include <cmath>
#include <cstdlib>
namespace java{ namespace lang {
class Math {
public:
    static int max(int a, int b) { return a > b ? a : b; }
    static int min(int a, int b) { return a < b ? a : b; }
    static long max(long a, long b) { return a > b ? a : b; }
    static long min(long a, long b) { return a < b ? a : b; }
    static double max(double a, double b) { return a > b ? a : b; }
    static double min(double a, double b) { return a < b ? a : b; }
    static int abs(int v) { return v < 0 ? -v : v; }
    static double abs(double v) { return std::fabs(v); }
    static double pow(double a, double b) { return std::pow(a, b); }
    static double sqrt(double v) { return std::sqrt(v); }
    static double floor(double v) { return std::floor(v); }
    static double ceil(double v) { return std::ceil(v); }
    static double random() { return static_cast<double>(std::rand()) / (RAND_MAX + 1.0); }
    static double PI;
    static double E;
};
inline double Math::PI = 3.141592653589793;
inline double Math::E = 2.718281828459045;
}}
