package base;

public class Inner1 {

    static int f11;
    int f12;
    Inner2 f13;

    static void m11() {
    }

    void print() {
        m11();
        f13 = new Inner2();
        f13.m21();
    }

    class Inner2 {
        static int f22;
        int f21;

        void m21() {
            m11();
            print();//ref.print
            f11 = 555;//ref.f11
            f13.m21();//ref.f13.m21
            m22();

            Inner1 par = Inner1.this;
        }

        void m22() {
        }

        static class Inner4 {
            void m41() {
                f11 = 1;
                f22 = 2;
            }
        }

        class Inner3 {
            int f31;

            void m31() {
                f11 = 1;
                f21 = 5;
                f31 = 10;
                print();
                Inner1 par = Inner1.this;
            }
        }

    }
}

