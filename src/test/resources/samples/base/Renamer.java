package base;

class Renamer {
    static final int bool = 1;
    int auto;
    int mem;

    void useField() {
        auto = 6;
        this.auto = 6;
    }

    void useStatic() {
        int a = bool;
        int b = Renamer.bool;
    }

    void mem() {

    }

    void useMem() {
        mem = 5;
        this.mem = 5;
    }

    void useObject() {
        Renamer r = this;
        r.auto = 5;
        r.mem = 6;
    }

    void useParam(int typename) {
        typename = 5;
    }
}