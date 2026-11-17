package com.mesut;

import org.junit.Test;

public class TryTest {

    @Test
    public void inner() {
        int x = 5;
        String str = "str";
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println(x);
            }
        };
        runnable.run();
    }

    @Test
    public void test() throws Exception {
        System.out.println(aa());
    }

    int print() {
        System.out.println("print");
        return 11;
    }

    int aa() throws Exception {
        try {
            return print();
        } finally {
            System.out.println("finally");
            throw new Exception("index");
        }
    }
}
