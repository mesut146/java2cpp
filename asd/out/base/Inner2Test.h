#pragma once

namespace base{

class Inner2Test: public java::lang::Object{
public:
    static int x_static;
    int x;
    base::Inner2Test_Inner1* obj1;
    base::Inner2Test_Inner1_Inner2* obj2;
    static void print_static();
    void print();
};
}
