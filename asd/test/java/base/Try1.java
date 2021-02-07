//package base;

public class Try1 {

    void withCatch() {
        try {
            if (1 == 1) {
                throw new RuntimeException("err");
            }
            int inTry;
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            int c2;
        }
        catch (Exception e) {
            e.printStackTrace();
            int c1;
        }
    }

    String with_finally() {
        try {
            if (1 == 1) {
                return "in try";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "in catch";
        } finally {
            return "in finally";
        }
    }

    void no_catch() {
        try
        {
            int x;
            return;
        } finally {
            int y;
        }
    }
}
