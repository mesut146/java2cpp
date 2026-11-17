package base;

public class Ops {

    int rshift(int a, int b) {
        return a >>> b;
    }

    int rshift2(int a, int b) {
        a >>>= b;
        return a;
    }

    long rshift3(long a, int b) {
        return a >>> b;
    }

    int rshift4(short a, int b) {
        return a >>> b;
    }

}