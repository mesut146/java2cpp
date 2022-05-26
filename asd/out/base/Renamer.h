#pragma once

namespace base{

class Renamer: public java::lang::Object{
public:
    static constexpr int bool = 1;
    int auto;
    int mem;
    void useField();
    void useStatic();
    void mem();
    void useMem();
    void useObject();
    void useParam(int typename);
};
}
