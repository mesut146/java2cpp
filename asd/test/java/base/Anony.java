package base;

class Anony {
    int val = 5;

    void test() {
        new a() {
            @java.lang.Override
            public void aa() {
                int tmp = val;
            }
        }
    }

    interface a {
        void aa();
    }
}