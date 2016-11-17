package org.simonis;

public class ArrayCopy {

  public static boolean arraycopy(Object[] src, int length) {
    try {
      System.arraycopy(src, 1, new Object[8], 1, length);
      return false;
    } catch (IndexOutOfBoundsException e) {
      return true;
    }
  }

  public static void main(String args[]){
    int count = Integer.parseInt(args[0]);

    for (int x = 0; x < count; x++) {
      if (arraycopy(new Object[8], -1) == false)
        throw new RuntimeException("Expected IndexOutOfBoundsException for System.arracopy(.., -1)");
    }
  }
}
