package base;


class SuperBase {
    void basePrint() {
    }
}

public class SuperTest extends SuperBase {

    int val;

    void print() {
    }

    void m11() {
    }

    class qualifiedInner {
        void call() {
            SuperTest.super.basePrint();
        }
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
