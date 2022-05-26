#pragma once

namespace base{

class Cast: public java::lang::Object{
public:
    void static_cast(base::Cast_Derived* d);
    void dyn_cast(base::Cast_Base* b);
    void cross(base::Cast_Derived2* d);
};
}
