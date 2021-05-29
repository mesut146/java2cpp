package base;

public class Cast {

    /*void static_cast(Derived d) {
        Base1 base1 = d;
    }*/

    void dyn_cast(cst d) {
        bearer base1 = (bearer) d;
    }

    static interface bearer {
    }

    static class cst {
    }

    /*static class Derived extends Base1 {

    }*/

    /*static class Derived2 extends Base1 implements Base2 {

    }*/

}