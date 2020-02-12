package base;

public class a extends Object implements Runnable {

    public int myInt = 5, eint;
    private static final Runnable r = null;
    String str[][] = {{"test"}, {"asd"}};
    long mylong[];
    short myshort[][];
    char mychar;
    byte mybyte;
    float myFloat;
    double myDouble;
    Object myObj = new second();

    class inner {
        int inner_field = 4;

        void inner_meth() {
        }

        class inner_inner {
            void inner_inner_method() {
            }
        }
    }

    public static void main() {
        System.out.println("Hello World!");
        //System::out->println();
    }

    String[] asd(int i, String[][] s) {
        return null;
    }

    int[][] sec(int[] asd) {
        return null;
    }

    @Override
    public void run() {

    }

}

class second {
    int second_field = 55555;
}
