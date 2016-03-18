package org.simonis;

public class LoopWithGC {

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

    new Thread() {
      { setDaemon(true); }
      public void run() {
        while (true) {
          try {
            Thread.sleep(1_000);
          } catch (InterruptedException e) {}
          System.gc();
        }
      }
    }.start();

    compute(count, seed);
  }
}
