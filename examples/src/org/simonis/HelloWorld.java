package org.simonis;

import java.text.NumberFormat;

public class HelloWorld {

    static public void sayHello() {
        System.out.println("Привет JBreak!");
    }
    public static void main(String ... args) {
        //System.out.println();
        long start = System.nanoTime();
        sayHello();
        long stop = System.nanoTime();
        System.out.format("%,9d%s%n", stop - start, "ns");

//        for (int i = 0; i < 200_000; i++) {
//            sayHello();
//        }
    }
}
