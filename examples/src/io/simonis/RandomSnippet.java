package io.simonis;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import jdk.vm.ci.panama.MachineCodeSnippet;

import static jdk.vm.ci.panama.MachineCodeSnippet.requires;
import static jdk.vm.ci.panama.MachineCodeSnippet.Effect.*;
import static jdk.vm.ci.amd64.AMD64.CPUFeature.*;
import static jdk.vm.ci.amd64.AMD64.*;

public class RandomSnippet {

  static final MethodHandle rdrand = MachineCodeSnippet.builder("rdrand")
    .returns(int.class, rax)
    .kills(rax)
    .code(0x0F, 0xC7, 0xF0)   // rdrand eax
    .make();

  private static int rdrand() {
    try {
      return (int)rdrand.invokeExact();
    } catch (Throwable e) {
      throw new Error(e);
    }
  }

  /*

  # {method} {0x00007fffd3a0f6c0} 'foo' '()I' in 'io/simonis/RandomSnippet'
  #           [sp+0x20]  (sp of caller)
  0x00007fffe49ff240: mov    %eax,-0x16000(%rsp)
  0x00007fffe49ff247: push   %rbp
  0x00007fffe49ff248: sub    $0x10,%rsp         ;*synchronization entry
                                                ; - io.simonis.RandomSnippet::foo@-1 (line 30)

 ;; snippet "rdrand" {
  0x00007fffe49ff24c: rdrand %eax               ;*invokestatic linkToNative {reexecute=0 rethrow=0 return_oop=0}
                                                ; - java.lang.invoke.LambdaForm$MH/0x0000000800066440::invoke@15
                                                ; - java.lang.invoke.Invokers$Holder::invokeExact_MT@18
                                                ; - io.simonis.RandomSnippet::rdrand@3 (line 23)
                                                ; - io.simonis.RandomSnippet::foo@0 (line 30)

 ;; } snippet "rdrand"
  0x00007fffe49ff24f: add    $0x10,%rsp
  0x00007fffe49ff253: pop    %rbp
  0x00007fffe49ff254: mov    0x100(%r15),%r10
  0x00007fffe49ff25b: test   %eax,(%r10)        ;   {poll_return}
  0x00007fffe49ff25e: retq

   */
  static int foo() {
    return rdrand();
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
