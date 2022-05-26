#pragma once

namespace base{

class Generic: public base::type1{
public:
    void meth(java::lang::Object* param, java::lang::Object* other);
    void wild(java::util::List* list);
    void wild2(java::util::List* list);
};
}
