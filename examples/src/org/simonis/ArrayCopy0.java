package org.simonis;

public class ArrayCopy0 {

  public static boolean arraycopy(Object[] src, int length) {
    try {
      System.arraycopy(src, 0, new Object[8], 1, length);
      return false;
    } catch (IndexOutOfBoundsException e) {
      return true;
    }
  }

  public static void main(String args[]){
    int count = args.length > 0 ? Integer.parseInt(args[0]) : 1;
    Object[] src = new Object[8];
    for (int x = 0; x < count; x++) {
      if (!arraycopy(src, -1))
        throw new RuntimeException("Expected IndexOutOfBoundsException for System.arracopy(.., -1)");
    }
  }
}
