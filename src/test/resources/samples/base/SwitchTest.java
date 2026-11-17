package base;

public class SwitchTest {

    int f = 5;

    int val() {
        return 5;
    }

    void enuma(all type) {
        switch (type) {
            case TWO:
            case FOUR:
                System.out.println("even");
                break;
            case ONE:
            case THREE:
                System.out.println("odd");
                break;
        }
    }

    void test() {
        int p = val();
        switch (val()) {
            case 1:
                return;
            case 2: {

            }
            case 3:
                int x;
                break;
            case 4:
            case 5:
                int y;
            case 6:
            case 7: {
                int z;
            }
            default:
                return;
        }

    }

    enum all {
        ONE, TWO, THREE, FOUR, FIVE;
    }

}
