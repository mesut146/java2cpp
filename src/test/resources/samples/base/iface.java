package base;

public interface iface extends base {
    int x = 8;
    base obj = null;

    void asd();

    default String name() {
        return "iface:" + x;
    }

    static iface create() {
        return new test();
    }
}

interface base {
    void baseMeth();
}

class test implements iface {
    public void asd() {

    }

    public void baseMeth() {

    }

}
