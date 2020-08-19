package com.mesut;

import org.junit.Test;

public class TryTest {

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
