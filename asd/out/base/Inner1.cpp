using namespace base;

//class Inner1
void Inner1::m11(){
}
void Inner1::print(){
    base::Inner1::m11();
    f13=new Inner1_Inner2(, this);
    f13->m21();
}
