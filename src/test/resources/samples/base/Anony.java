package base;

class Anony {
    int val = 5;

    void test(final Anony asd) {
        final int local = 10;
        new a() {
            @Override
            public void aa() {
                int tmp = val + local;
                int tmp2 = val + local + 1;
                asd.val = 5;
            }
        };
    }

    void test2() {
        final int local = 10;
        new inner(5, 6) {
            @java.lang.Override
            void calc() {
                int tmp = local + 1;
            }
        };
    }

    interface a {
        void aa();
    }

    static class inner {
        public inner(int val, int sec) {

        }

        void calc() {

        }
    }
}