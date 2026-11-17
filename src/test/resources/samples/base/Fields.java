package base;

class type {
}

public class Fields {
    static type st_obj = new type();
    type obj = new type();

    Fields(int dummy) {
        this(new type());
    }

    Fields(type p) {
    }

    Fields() {
    }

}
