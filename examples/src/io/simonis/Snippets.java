package io.simonis;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import jdk.vm.ci.panama.MachineCodeSnippet;

import static jdk.vm.ci.panama.MachineCodeSnippet.requires;
import static jdk.vm.ci.panama.MachineCodeSnippet.Effect.*;
import static jdk.vm.ci.amd64.AMD64.CPUFeature.*;
import static jdk.vm.ci.amd64.AMD64.*;

public class Snippets {
  /*
  static final MethodHandle rdrand = MachineCodeSnippet.make("rdrand",
    MethodType.methodType(long.class),  // return type (no parameters)
    effects(READ_MEMORY, WRITE_MEMORY), // RW
    requires(AVX),
    0xC4, 0xE1, 0x7E, 0x6F, 0x04, 0x37,  // vmovdqu ymm0,[rsi+rdi]
    0xC4, 0xE1, 0x7E, 0x7F, 0x04, 0x0A); // vmovdqu [rdx+rcx],ymm0
  */
  static final MethodHandle rdrand = MachineCodeSnippet.builder("rdrand")
    .effects(/*no effects*/)
    .returns(int.class, rax)
    .kills(rax)
    .code(0x0F, 0xC7, 0xF0)   // rdrand eax
    .make();

  private static int rdrand() {
    try {
      return (Integer)rdrand.invoke();
    } catch (Throwable e) {
      throw new Error(e);
    }
  }

  public static void main(String[] args) {
    System.out.println(rdrand());
  }
}
