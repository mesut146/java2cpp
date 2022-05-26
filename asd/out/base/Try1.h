#pragma once

namespace base{

class Try1: public java::lang::Object{
public:
    void withCatch();
    java::lang::String* with_finally();
    void no_catch();
};
}
