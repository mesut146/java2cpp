package base;

public class Inner2Test {

    static int x_static = 22;
    int x = 123456;
    Inner1 obj1;
    Inner1.Inner2 obj2;

    static void print_static() {
    }

    void print() {
        obj1 = new Inner1();
    }

    class Inner1 {
        void innerMeth() {
            obj2 = new Inner2();
        }

        class Inner2 {

            void innerMeth2() {
                print();//ref.ref.print()
                innerMeth();//ref.innerMeth()
                x = 5;//ref.ref.x
            }
        }

    }
}

