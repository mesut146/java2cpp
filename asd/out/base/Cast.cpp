using namespace base;

//class Cast
void Cast::static_cast(Cast_Derived* d){
    Cast_Base* base = d;
}
void Cast::dyn_cast(Cast_Base* b){
    auto d = dynamic_cast<Cast_Derived*>(b);
}
void Cast::cross(Cast_Derived2* d){
    auto b = (Cast_Base*)d;
    auto i = dynamic_cast<Cast_iface*>(b);
}
