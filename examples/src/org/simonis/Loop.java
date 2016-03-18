package org.simonis;

public class Loop {

  static double compute(int count, double d) {
    for (int i = 0; i < count; i++) {
      d += Math.pow(Math.sin(d), Math.sqrt(d));
    }
    return d;
  }

  public static void main(String[] args) throws Exception {
    double seed = Double.parseDouble(args[0]);
    int warmup = Integer.parseInt(args[1]);
    int count = Integer.parseInt(args[2]);

    for (int i = 0; i < 20_000; i++) {
      compute(warmup, seed);
    }

    compute(count, seed);
  }
}
