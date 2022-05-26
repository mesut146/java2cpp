#pragma once

class Enum1: public java::lang::Enum{
public:
    static base::Enum1* m1;
    static base::Enum1* m2;
    java::lang::String* name;
    int x;
    void Enum1(java::lang::String* s);
    void Enum1();
    void print();
};