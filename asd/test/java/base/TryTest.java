public class TryTest {

    String normal() {
        try {
            if (1 == 1) {
                return "in try";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "in catch";
        }
        return "default";
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
            return;
        } finally {

        }
    }
}