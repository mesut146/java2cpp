package base;

public class Cast {

    void static_cast(Derived d) {
        Base base = d;
    }

    void dyn_cast(Base b) {
        Derived d = (Derived) b;
    }

    void cross(Derived2 d){
        Base b = (Base)d;
        iface i = (iface)b;
    }

    interface iface {
    }

    static class Base {
    }

    static class Derived extends Base {

    }

    static class Derived2 extends Base implements iface {

    }

}