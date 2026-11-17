package base;


public class SuperTest {

    int val;

    void print() {
    }

    void m11() {
    }

    static class inner extends SuperTest {

        @Override
        void print() {
        }

        void print2() {
            print();//this->print()
            super.print();//SuperTest::print
            this.print2();//this->print2()
            m11();//SuperTest::m11()
        }

        static class inner2 extends inner {
            void test() {
                val = 1;
                print();
                print2();
                super.print();

            }
        }
    }
}
