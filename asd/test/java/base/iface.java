package base;

public interface iface extends base {
    int x = 8;
    base obj = null;

    void asd();

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
