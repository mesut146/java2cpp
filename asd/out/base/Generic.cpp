using namespace base;

//class type1
void type1::add(java::lang::Object* elem, java::lang::Object* other){
}
//class type2
//class type3
//class Generic
void Generic::meth(java::lang::Object* param, java::lang::Object* other){
    auto obj = new type1();
    obj->add(param, other);
    add(param, other);
    java::util::List* l = nullptr;
    auto s = dynamic_cast<java::lang::String*>(l->get(0));
}
void Generic::wild(java::util::List* list){
    list->add(new type3());
    list->add(new type2());
    java::lang::Object* t2 = dynamic_cast<java::lang::Object*>(list->get(0));
}
void Generic::wild2(java::util::List* list){
    type2* t2 = dynamic_cast<type2*>(list->get(0));
}
