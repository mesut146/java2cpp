#pragma once

namespace base{

class Fields: public java::lang::Object{
public:
    static base::type* st_obj;
    base::type* obj;
    void Fields(int dummy);
    void Fields(base::type* p);
    void Fields();
};
}
