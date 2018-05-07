package io.simonis;

public class Loop {

  static double compute(int count, double d) {
    for (int i = 0; i < count; i++) {
      d += Math.pow(Math.sin(d), Math.sqrt(d));
    }
    return d;
  }

  public static void main(String[] args) throws Exception {
    double  seed = args.length;    // Just to avoid constant folding
    int    count = Integer.parseInt(args[0]); // Iteration count for compute()

    for (int i = 0; i < 20_000; i++) {
      compute(1, seed);            // Call compute() until it gets JIT-compiled
    }

    compute(count, seed);
  }
}
