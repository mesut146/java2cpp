package base;

public class a extends Object implements Runnable {

    public int myInt = 5, eint;
    private static final Runnable r = null;
    String str[][] = {{"test"}, {"asd"}};
    long mylong[];
    short myshort[][]=new short[10][20];
    int[][][][] arr4 = new int[1][2][3][str.length]; 
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

    public static void main(String[] args) {
        System.out.println("Hello World!");
        second sec = new second();
        int var = sec.second_field;
        String str = second.second_static;
        String norm = sec.second_normal;
        sec.second_normal_method();
        second.second_st_method();
        sec.second_st_method();
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

    public boolean is() {
        return this instanceof second;
    }

}

class second {
    int second_field = 55555;
    static String second_static = null;
    String second_normal = null;

    static void second_st_method() {
    }

    void second_normal_method() {
    }
}
