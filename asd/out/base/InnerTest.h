#pragma once

namespace base{

class InnerTest: public java::lang::Object{
public:
    int x;
    base::InnerTest_Inner* obj;
    base::InnerTest_Inner_Inner2* obj2;
    static int x_static;
    void print();
    static void print_static();
    void anonyTest();
    void anonyOther();
};
}
