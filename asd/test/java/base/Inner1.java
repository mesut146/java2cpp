package base;

public class Inner1 {

    static int x_static = 22;
    int x = 123456;
    Inner obj;

    static void print_static() {
    }

    void print() {
        obj = new Inner();
        obj.innerMeth();
    }

    class Inner {
        void innerMeth() {
            print();//ref.print
            x = 555;//ref.x
            obj.innerMeth();//ref.obj.innerMeth
            local();

            Inner1 par;
            par = Inner1.this;
        }

        void local() {
        }

        class ii {
            void aa() {
                Inner1 par;
                par = Inner1.this;
            }
        }

    }
}

