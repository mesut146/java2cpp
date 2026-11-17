package base;

class Test {

    void make() {
        String s = "asd " + 'c';
        s = s + "foo";
        s = s + 2;
    }

    void funcs(String s) {
        int hash = s.hashCode();
        int idx = s.indexOf("hello");
        int idx2 = s.indexOf('h');
        int idx3 = s.lastIndexOf("hello");
        int idx4 = s.lastIndexOf('h');

    }

}