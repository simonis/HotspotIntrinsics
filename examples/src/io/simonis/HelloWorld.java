package io.simonis;

import java.text.NumberFormat;

public class HelloWorld {

    public static void sayHello() {
        System.out.println("Hello GeeCON!");
    }
    public static void main(String ... args) {

        long start = System.nanoTime();
        sayHello();
        long stop = System.nanoTime();
        System.out.format("%,9d%s%n", stop - start, "ns");

    }
}
