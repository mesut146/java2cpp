#pragma once

namespace base{

class Box: public java::lang::Object{
public:
    java::lang::Boolean* box();
    bool unbox(java::lang::Integer* i, java::lang::Boolean* b);
};
}
