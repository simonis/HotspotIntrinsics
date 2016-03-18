package org.simonis;

import java.security.SecureRandom;

public class Random {

  private static final SecureRandom sr = new SecureRandom();

  static int foo() {
    return sr.nextInt();
  }

  public static void main(String[] args) throws Exception {
    int count = Integer.parseInt(args.length > 0 ? args[0] : "10");
    int result = 0;
    for (int i = 0; i < count; i++) {
      result += foo();
    }
    System.out.println(result);
  }

}
