package base;

public enum Enum1 {
    m1("m111"), m2;

    String name;
    int x = 5;

    Enum1(String s) {
        name = s;
    }

    Enum1() {
        this("def");
    }

    void print() {

    }
}
