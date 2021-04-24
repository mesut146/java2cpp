package base;

class SuperCall {

    SuperCall(int x) {

    }

    static class type<T> {

    }

    static class inner extends SuperCall {
        inner() {
            super(5);
        }
    }

    static class inner2 extends type<SuperCall> {
        inner2() {

        }
    }

}