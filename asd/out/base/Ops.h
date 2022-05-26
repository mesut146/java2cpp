#pragma once

namespace base{

class Ops: public java::lang::Object{
public:
    int rshift(int a, int b);
    int rshift2(int a, int b);
    long rshift3(long a, int b);
    int rshift4(char16_t a, int b);
};
}
