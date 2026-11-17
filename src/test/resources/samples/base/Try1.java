package base;

public class Try1 {

    void withCatch() throws Exception{
        try {
            if (1 == 1) {
                throw new RuntimeException("err");
            }
            int inTry;
        } catch (RuntimeException re) {
            re.printStackTrace();
            Exception exc = re;
            throw exc;
        } catch (Exception ex) {
            ex.printStackTrace();
            RuntimeException re0 = new RuntimeException(ex);
            re0.printStackTrace();
            throw re0;
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
        try {
            int x;
            return;
        } finally {
            int y;
        }
    }
}
