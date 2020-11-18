# VarHandle Intriniscs

The thing with `VarHandle::compareAndSet` is a little complicated As Paul mentioned, methods which want to get intensified do have to use the `@HotSpotIntrinsicCandidate` annotation. But that's only one direction. Methods which do have that annotation don't necessarily have to be intensified. Intrinsification can happen in any (and in any combination) of Interpreter, C1 or C2 although you usually start with a C2 intrinsic for peak performance. If Hotspot wants to intrinsify a method, it declares it in `vmSymbols.hpp` with the help of the `do_intrinsic(...)` macro. For more information about the implementation of intrinsics I shamelessly (re-)advertise my [talk on HotSpot intrinsics](https://2018.geekout.ee/volker-simonis/) 

For `VarHandle::compareAndSet` (and for most of the `MethodHandle`/`VarHandle` methods) this is a little different. As you saw, these methods are annotated with `@HotSpotIntrinsicCandidate` but they are also `native`. However you won't be able to find a native implementation of these methods in the source code. This is because their implementation requires some internal VM magic which is impossible to implement in native C or Java. Therefore, these methods **must** have to be intrinisfied by the VM. They are not even declared as intrinsics in `vmSymbols.hpp`. Instead they get a special treatment when they are loaded and their correponding methods get automatically registered as intrinsics:


```
ClassFileParser::fill_instance_klass(...)
  check_methods_for_intrinsics(...)
    void Method::init_intrinsic_id()
      vmIntrinsics::ID MethodHandles::signature_polymorphic_name_id(Klass* klass, Symbol* name)
        vmIntrinsics::ID MethodHandles::signature_polymorphic_name_id(Symbol* name)
```

One of the reasons why these methods can't be implemented in Java or JNI/C is the fact that they are [signature polymorphic](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/invoke/MethodHandle.html#sigpoly). This basically means that they can be called with an arbitrary number and types of arguments. Javac will simply emit an invokevirtual call to a method with the signature derived from the arguments at the call site (and not the one from the method declaration). This requires some special handling in the VM.When the VM parses an invokevirtual call to a signature polymorphic method (e.g. `VarHandle.compareAndSet`) at class loading time, it rewrites the invokevirtual bytecode with an artificial **invokehandle** bytecode (see `Rewriter::maybe_rewrite_invokehandle()`). When this invokehandle is executed for the first time, it will set up a proper call to an helper function which does two things. First it validates the dynamic arguments on the stack with the static method signature and second it invokes a helper function which executes the requested functionality. Let's see how this works in a simple example:


```
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;public class VarHandleTestSimple {
    static int i;
    static final VarHandle vh;
    static {
        VarHandle tmp;
        try {
            tmp = MethodHandles.lookup().findStaticVarHandle(VarHandleTestSimple.class, "i", int.class);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            tmp = null;
        }
        vh = tmp;
    }    public static void foo(int i) throws Exception {
        vh.compareAndSet(0, i);
    }    public static void main(String[] args) throws Exception {
        for (int i=0; i < 10_000; i++) {
            foo(42);
        }
    }
}
```

If we run this with `-Djava.lang.invoke.MethodHandle.TRACE_METHOD_LINKAGE=true` we will see that `VarHandle.compareAndSet` will be actually linked against `java.lang.invoke.VarHandleGuards.guard_II_Z` (the helper method I mentioned before):


```
java -Djava.lang.invoke.MethodHandle.TRACE_METHOD_LINKAGE=true VarHandleTestSimple
linkMethod java.lang.invoke.VarHandle.compareAndSet(int,int)boolean/5
linkMethod => java.lang.invoke.VarHandleGuards.guard_II_Z(VarHandle,int,int,AccessDescriptor)boolean/invokeStatic + java.lang.invoke.VarHandle$AccessDescriptor@3764951d
```

You can use -Xlog:methodhandles+indy=debug to get even more information on how the method was linked at run time:

```
$ java -Djava.lang.invoke.MethodHandle.TRACE_METHOD_LINKAGE=true -Xlog:methodhandles+indy=debug VarHandleTestSimple
[0,154s][debug][methodhandles,indy] MethodHandle support loaded (using LambdaForms)
linkMethod java.lang.invoke.VarHandle.compareAndSet(int,int)boolean/5
linkMethod => java.lang.invoke.VarHandleGuards.guard_II_Z(VarHandle,int,int,AccessDescriptor)boolean/invokeStatic + java.lang.invoke.VarHandle$AccessDescriptor@3764951d
[0,242s][debug][methodhandles,indy] set_method_handle bc=233 appendix=0x000000062b84bae0 method=0x00000008001afe88 (local signature) 
[0,242s][debug][methodhandles,indy] {method}
[0,242s][debug][methodhandles,indy]  - this oop:          0x00000008001afe88
[0,242s][debug][methodhandles,indy]  - method holder:     'java/lang/invoke/VarHandleGuards'
[0,242s][debug][methodhandles,indy]  - constants:         0x00000008007b9c30 constant pool [743] {0x00000008007b9c30} for 'java/lang/invoke/VarHandleGuards' cache=0x00000008001adb68
[0,242s][debug][methodhandles,indy]  - access:            0x18  static final 
[0,242s][debug][methodhandles,indy]  - name:              'guard_II_Z'
[0,242s][debug][methodhandles,indy]  - signature:         '(Ljava/lang/invoke/VarHandle;IILjava/lang/invoke/VarHandle$AccessDescriptor;)Z'
[0,242s][debug][methodhandles,indy]  - max stack:         6
[0,242s][debug][methodhandles,indy]  - max locals:        5
[0,242s][debug][methodhandles,indy]  - size of params:    4
[0,242s][debug][methodhandles,indy]  - method size:       13
[0,242s][debug][methodhandles,indy]  - intrinsic id:      315 _compiledLambdaForm
[0,242s][debug][methodhandles,indy]  - vtable index:      -2
[0,242s][debug][methodhandles,indy]  - i2i entry:         0x0000000800000010
[0,242s][debug][methodhandles,indy]  - adapters:          AHE@0x00007ffff0188370: 0xbaab0000 i2c: 0x00007fffd79a9560 c2i: 0x00007fffd79a9628 c2iUV: 0x00007fffd79a95ee c2iNCI: 0x00007fffd79a9665
[0,242s][debug][methodhandles,indy]  - compiled entry     0x00000008000041a8
[0,242s][debug][methodhandles,indy]  - code size:         73
[0,242s][debug][methodhandles,indy]  - code start:        0x00000008007bd780
[0,242s][debug][methodhandles,indy]  - code end (excl):   0x00000008007bd7c9
[0,242s][debug][methodhandles,indy]  - checked ex length: 1
[0,242s][debug][methodhandles,indy]  - checked ex start:  0x00000008007bd814
[0,242s][debug][methodhandles,indy]  - linenumber start:  0x00000008007bd7c9
[0,242s][debug][methodhandles,indy]  - localvar length:   5
[0,242s][debug][methodhandles,indy]  - localvar start:    0x00000008007bd7d6
[0,242s][debug][methodhandles,indy] java.lang.invoke.VarHandle$AccessDescriptor 
[0,242s][debug][methodhandles,indy] {0x000000062b84bae0} - klass: 'java/lang/invoke/VarHandle$AccessDescriptor'
[0,242s][debug][methodhandles,indy]  - ---- fields (total size 4 words):
[0,242s][debug][methodhandles,indy]  - final 'type' 'I' @12  2
[0,242s][debug][methodhandles,indy]  - final 'mode' 'I' @16  8
[0,242s][debug][methodhandles,indy]  - final 'symbolicMethodTypeErased' 'Ljava/lang/invoke/MethodType;' @20  a 'java/lang/invoke/MethodType'{0x000000062b846e78} = (II)Z (c5708dcf)
[0,242s][debug][methodhandles,indy]  - final 'symbolicMethodTypeInvoker' 'Ljava/lang/invoke/MethodType;' @24  a 'java/lang/invoke/MethodType'{0x000000062b84bb38} = (Ljava/lang/invoke/VarHandle;II)Z (c5709767)
[0,242s][debug][methodhandles,indy]  - final strict 'returnType' 'Ljava/lang/Class;' @28  a 'java/lang/Class'{0x00000007ffb00000} = boolean (fff60000)
[0,242s][debug][methodhandles,indy]                  -------------
[0,242s][debug][methodhandles,indy]   0  (0x00007fffb55f87c8)  [00|e9|    3]
[0,242s][debug][methodhandles,indy]                  [   0x00000008001afe88]
[0,242s][debug][methodhandles,indy]                  [   0x0000000000000001]
[0,242s][debug][methodhandles,indy]                  [   0x0000000013400004]
[0,242s][debug][methodhandles,indy]                  -------------
```

You can use `-XX:+TraceBytecodes` to understand what's happening at runtime:

```
$ java -Djava.lang.invoke.MethodHandle.TRACE_METHOD_LINKAGE=true -Xlog:methodhandles+indy=debug -XX:+TraceBytecodes VarHandleTestSimple 
...
[23826] static void VarHandleTestSimple.foo(jint)
[23826]   462654     0  getstatic 2 <VarHandleTestSimple.vh/Ljava/lang/invoke/VarHandle;> 
[23826]   462655     3  iconst_0
[23826]   462656     4  iload_0
[23826]   462657     5  invokehandle 3 <java/lang/invoke/VarHandle.compareAndSet(II)Z> [23826] virtual jobject java.lang.ClassLoader.loadClass(jobject)
[23826]   462658     0  nofast_aload_0
...
[23826] static jobject java.lang.invoke.MethodHandleNatives.findMethodHandleType(jobject, jobject)
[23826]   467313     0  nofast_aload_0
[23826]   467314     1  aload_1
[23826]   467315     2  iconst_1
[23826]   467316     3  invokestatic 233 <java/lang/invoke/MethodType.makeImpl(Ljava/lang/Class;[Ljava/lang/Class;Z)Ljava/lang/invoke/MethodType;> [23826] static jobject java.lang.invoke.MethodType.makeImpl(jobject, jobject, jboolean)
...
[23826] static jboolean java.lang.invoke.VarHandleGuards.guard_II_Z(jobject, jint, jint, jobject)
...
[23826] static jboolean java.lang.invoke.VarHandleInts$FieldStaticReadWrite.compareAndSet(jobject, jint, jint)
[23826]   494015     0  aload_0
[23826]   494016     1  checkcast 2 <java/lang/invoke/VarHandleInts$FieldStaticReadWrite>
[23826]   494017     4  astore_3
[23826]   494018     5  getstatic 13 <java/lang/invoke/MethodHandleStatics.UNSAFE/Ljdk/internal/misc/Unsafe;> 
[23826]   494019     8  aload_3
[23826]   494020     9  getfield 19 <java/lang/invoke/VarHandleInts$FieldStaticReadWrite.base/Ljava/lang/Object;> 
[23826]   494021    12  aload_3
[23826]   494022    13  getfield 23 <java/lang/invoke/VarHandleInts$FieldStaticReadWrite.fieldOffset/J> 
[23826]   494023    16  iload_1
[23826]   494024    17  iload_2
[23826]   494025    18  invokevirtual 42 <jdk/internal/misc/Unsafe.compareAndSetInt(Ljava/lang/Object;JII)Z> 
[23826]   494026    21  ireturn[23826] static jboolean java.lang.invoke.VarHandleGuards.guard_II_Z(jobject, jint, jint, jobject)
[23826]   494027    43  ireturn
```

At the first invokation of `VarHandleTestSimple.foo` the `invokevirtual` call to `VarHandle.compareAndSet` is replaced by the synthetic `invokehandle` bytecode which does the runtime linking and invokes the corresponding helper method `java.lang.invoke.VarHandleGuards.guard_II_Z` which in the end calls `Unsafe.compareAndSetInt` (which is itself a native intrinisc). When foo is called for the second time, the linking isn't necessary any more and we directly call `VarHandleGuards.guard_II_Z` :


```
[23826] static void VarHandleTestSimple.foo(jint)
[23826]   494037     0  getstatic 2 <VarHandleTestSimple.vh/Ljava/lang/invoke/VarHandle;> 
[23826]   494038     3  iconst_0
[23826]   494039     4  iload_0
[23826]   494040     5  invokehandle 3 <java/lang/invoke/VarHandle.compareAndSet(II)Z> [23826] static jboolean java.lang.invoke.VarHandleGuards.guard_II_Z(jobject, jint, jint, jobject)
...
[23826] static jboolean java.lang.invoke.VarHandleGuards.guard_II_Z(jobject, jint, jint, jobject)
[23826]   494085    43  ireturn[23826] static void VarHandleTestSimple.foo(jint)
[23826]   494086     8  pop
[23826]   494087     9  return
```

Now we know how this is all handled in the Interpreter. But what happens once the JIT kicks in:

```
$ java -Djava.lang.invoke.MethodHandle.TRACE_METHOD_LINKAGE=true -Xlog:methodhandles+indy=debug -Xbatch -XX:-TieredCompilation -XX:CICompilerCount=1 -XX:CompileCommand="option,VarHandleTestSimple::foo,PrintOptoAssembly" -XX:CompileCommand="option,VarHandleTestSimple::foo,PrintInlining" -XX:-UseCompressedOops -XX:-UseOnStackReplacement VarHandleTestSimple------------------------ OptoAssembly for Compile_id = 4 -----------------------
#
#  void ( int )
#
#r018 rsi   : parm 0: int
# -- Old rsp -- Framesize: 32 --
#r583 rsp+28: in_preserve
#r582 rsp+24: return address
#r581 rsp+20: in_preserve
#r580 rsp+16: saved fp register
#r579 rsp+12: pad2, stack alignment
#r578 rsp+ 8: pad2, stack alignment
#r577 rsp+ 4: Fixed slot 1
#r576 rsp+ 0: Fixed slot 0
#
000     N1: #    out( B1 ) <- in( B1 )  Freq: 1000     B1: #    out( N1 ) <- BLOCK HEAD IS JUNK  Freq: 1
000     # stack bang (96 bytes)
    pushq   rbp    # Save rbp
    subq    rsp, #16    # Create frame00c     MEMBAR-release ! (empty encoding)
00c     
00c     xorl    RAX, RAX    # int
00e     movq    R10, java/lang/Class:exact *    # ptr
018     cmpxchgl [R10 + #184 (32-bit)],RSI    # If rax == [R10 + #184 (32-bit)] then store RSI into [R10 + #184 (32-bit)]
    sete    R10
    movzbl  R10, R10
029     
029     MEMBAR-acquire ! (empty encoding)
029     addq    rsp, 16    # Destroy frame
    popq    rbp
    cmpq    poll_offset[r15_thread], rsp
    ja      #safepoint_stub    # Safepoint: poll for GC03b     ret--------------------------------------------------------------------------------
                            @ 5   java.lang.invoke.VarHandleGuards::guard_II_Z (73 bytes)   force inline by annotation
                              @ 1   java.lang.invoke.VarHandle::isDirect (2 bytes)   inline (hot)
                              @ 37   java.lang.invoke.VarForm::getMemberName (21 bytes)   force inline by annotation
                              @ 40   java.lang.invoke.VarHandleInts$FieldStaticReadWrite::compareAndSet (22 bytes)   force inline by annotation
                                @ 18   jdk.internal.misc.Unsafe::compareAndSetInt (0 bytes)   (intrinsic)
```

Notice how everything up to Unsafe::compareAndSetInt is nicely inlined and the whole call to VarHandle.compareAndSet is collapsed into a single cmpxchg instruction:

```
00c     MEMBAR-release ! (empty encoding)
00c     
00c     xorl    RAX, RAX    # int
00e     movq    R10, java/lang/Class:exact *    # ptr
018     cmpxchgl [R10 + #184 (32-bit)],RSI    # If rax == [R10 + #184 (32-bit)] then store RSI into [R10 + #184 (32-bit)]
    sete    R10
    movzbl  R10, R10
029     
029     MEMBAR-acquire ! (empty encoding)
```

The membars are empty on x86.For this to happen, **it is essential, that the VarHandle is final** (i.e. can be treated as a constant by the JIT). If this is not the case, the VarHandle won't be inlined and you'll get a very bad performance:

```
                            @ 5   java.lang.invoke.VarHandleGuards::guard_II_Z (73 bytes)   force inline by annotation
                              @ 1   java.lang.invoke.VarHandle::isDirect (2 bytes)   inline (hot)
                              @ 37   java.lang.invoke.VarForm::getMemberName (21 bytes)   force inline by annotation
                              @ 40   java.lang.invoke.MethodHandle::linkToStatic(LIIL)I (0 bytes)   member_name not constant
```

So to cut a long story short and answer your question: yes, `VarHandle.compareAndSet()` for an int fiels ends up in `Unsafe.compareAndSetInt()`, but maybe not in the most obvious way 


# CRC32 Intriniscs

Not sure how important `vmIntrinsics::_updateCRC32` really is as it is for `CRC32.update(int crc, int b)` which updates the CRC for a single byte. I think if you really want performance you will use the array/bytebuffer version anyway.That said, it would be not trivial to use crc32b in an intrinsic for `vmIntrinsics::_updateCRC32` because of the way how it is currently implemented.For **C2** the `vmIntrinsics::_updateBytesCRC32` and `vmIntrinsics::_updateByteBufferCRC32` intrinsics are implemented in shared code as a call to the corresponding stubs:

```
// src/hotspot/share/opto/library_call.cpp
bool LibraryCallKit::inline_updateBytesCRC32() {
  address stubAddr = StubRoutines::updateBytesCRC32();
  Node* call = make_runtime_call(RC_LEAF|RC_NO_FP, OptoRuntime::updateBytesCRC32_Type(),
                                 stubAddr, stubName, TypePtr::BOTTOM,
                                 crc, src_start, length);bool LibraryCallKit::inline_updateByteBufferCRC32() {
  address stubAddr = StubRoutines::updateBytesCRC32();
  Node* call = make_runtime_call(RC_LEAF|RC_NO_FP, OptoRuntime::updateBytesCRC32_Type(),
                                 stubAddr, stubName, TypePtr::BOTTOM,
                                 crc, src_start, length);
```

The stubs are genrerated here:

```
// src/hotspot/cpu/aarch64/stubGenerator_aarch64.cpp
  address generate_updateBytesCRC32() {
    assert(UseCRC32Intrinsics, "what are we doing here?");
    __ kernel_crc32(crc, buf, len,
              table0, table1, table2, table3, rscratch1, rscratch2, tmp3);
```

Depending on `UseCRC32`, `MacroAssembler::kernel_crc32` either dispatches to `MacroAssembler::kernel_crc32_using_crc32` which uses the ARM CRC instructions or generates a simple version which uses table lookup.For **C1**, the `vmIntrinsics::_updateBytesCRC32` and `vmIntrinsics::_updateByteBufferCRC32` intriniscs are implemented in cpu-specific code and also call the corresponding stubs. The intrinsic for `vmIntrinsics::_updateCRC32` creates a `LIR_OpUpdateCRC32` node (by calling `update_crc32`) which exands to `MacroAssembler::update_byte_crc32` in `LIR_Assembler::emit_updatecrc32(LIR_OpUpdateCRC32* op)` which in the end inline the assembly from `MacroAssembler::update_byte_crc32` :

```
// src/hotspot/cpu/aarch64/c1_LIRGenerator_aarch64.cpp
void LIRGenerator::do_update_CRC32(Intrinsic* x) {
  assert(UseCRC32Intrinsics, "why are we here?");
    case vmIntrinsics::_updateCRC32: {
      __ update_crc32(crc.result(), val.result(), result);
    case vmIntrinsics::_updateBytesCRC32:
    case vmIntrinsics::_updateByteBufferCRC32: {
      __ call_runtime_leaf(StubRoutines::updateBytesCRC32(), getThreadTemp(), result_reg, cc->args());// src/hotspot/cpu/aarch64/c1_LIRAssembler_aarch64.cpp
void LIR_Assembler::emit_updatecrc32(LIR_OpUpdateCRC32* op) {
  __ adrp(res, ExternalAddress(StubRoutines::crc_table_addr()), offset);
  __ update_byte_crc32(crc, val, res);
```

For the **TemplateInterpreter**, the `vmIntrinsics::_updateBytesCRC32` and `vmIntrinsics::_updateByteBufferCRC32` intriniscs are implemented in cpu-specific code and also call the corresponding stubs:

```
// src/hotspot/cpu/aarch64/templateInterpreterGenerator_aarch64.cpp
address TemplateInterpreterGenerator::generate_CRC32_updateBytes_entry(AbstractInterpreter::MethodKind kind) {
  if (UseCRC32Intrinsics) {
    // We are frameless so we can just jump to the stub.
    __ b(CAST_FROM_FN_PTR(address, StubRoutines::updateBytesCRC32()));
```

while the `vmIntrinsics::_updateCRC32` intrinisc directly inlines the assembly from `MacroAssembler::update_byte_crc32` :

```
// src/hotspot/cpu/aarch64/templateInterpreterGenerator_aarch64.cpp
address TemplateInterpreterGenerator::generate_CRC32_update_entry() {
  if (UseCRC32Intrinsics) {
    __ adrp(tbl, ExternalAddress(StubRoutines::crc_table_addr()), offset);
    __ update_byte_crc32(crc, val, tbl);
```

Now you can easily create a new version `MacroAssembler::update_byte_crc32` , lets say `MacroAssembler::update_byte_crc32_with_crcb` and use this one from `TemplateInterpreterGenerator::generate_CRC32_update_entry()` and `LIR_Assembler::emit_updatecrc32(LIR_OpUpdateCRC32* op)` if `UseCRC ==true` (in that case you also wouldn't have to load the crc table).Unfortunately things are not so easy for the **C2** intrinisc for `vmIntrinsics::_updateBytesCRC32` (which is the most important one performance-wise) because this one is imlemented completey in shared code by rewriting the Ideal Graph:

```
// src/hotspot/share/opto/library_call.cpp
bool LibraryCallKit::inline_updateCRC32() {
  assert(UseCRC32Intrinsics, "need AVX and LCMUL instructions support");
  assert(callee()->signature()->size() == 2, "update has 2 parameters");
  // no receiver since it is static method
  Node* crc  = argument(0); // type: int
  Node* b    = argument(1); // type: int  /*
   *    int c = ~ crc;
   *    b = timesXtoThe32[(b ^ c) & 0xFF];
   *    b = b ^ (c >>> 8);
   *    crc = ~b;
   */  Node* M1 = intcon(-1);
  crc = _gvn.transform(new XorINode(crc, M1));
  Node* result = _gvn.transform(new XorINode(crc, b));
  result = _gvn.transform(new AndINode(result, intcon(0xFF)));  Node* base = makecon(TypeRawPtr::make(StubRoutines::crc_table_addr()));
  Node* offset = _gvn.transform(new LShiftINode(result, intcon(0x2)));
  Node* adr = basic_plus_adr(top(), base, ConvI2X(offset));
  result = make_load(control(), adr, TypeInt::INT, T_INT, MemNode::unordered);  crc = _gvn.transform(new URShiftINode(crc, intcon(8)));
  result = _gvn.transform(new XorINode(crc, result));
  result = _gvn.transform(new XorINode(result, M1));
  set_result(result);
  return true;
}
```

This basically emulates the assembler implementation from `MacroAssembler::update_byte_crc32` in a platform-independent way.You would first have to define a new, global option like `UseCRC32` (because `UseCRC32` is currently only defined for aarch64) which indicates the presence of a special CRC32 instruction. Then you need to implement a new Node for the C2 IdealGraph (e.g. CRC32Node) and conditionally create that one in `LibraryCallKit::inline_updateCRC32()` if `UseCRC32` is true. Finally, you would have to implement the code generation for the new node to simple call your new `MacroAssembler::update_byte_crc32_with_crcb` assembler function in `src/hotspot/cpu/aarch64/aarch64.ad`. You can see in my video on intrinisics how all this can be done in more detail.In the end it might be worth doing all this if we see that `CRC32.update(int crc, int b)` is used heavily (you can check the Amazon profiler for that method) and if the crc32b instruction is indeed significantly faster than the current table-based assembler implementation.
