package org.simonis;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

public class MethodInstrumentationAgent {

  static String pattern;

  public static void premain(String args, Instrumentation inst) {
    inst.addTransformer(new MethodInstrumentorTransformer());
    pattern = args;
  }

  static class MethodInstrumentorTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
      ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
      MethodInstrumentorClassVisitor cv = new MethodInstrumentorClassVisitor(cw);
      ClassReader cr = new ClassReader(classfileBuffer);
      cr.accept(cv, 0);
      return cw.toByteArray();
    }

  }

  static class MethodInstrumentorClassVisitor extends ClassVisitor {
    private String className;

    public MethodInstrumentorClassVisitor(ClassVisitor cv) {
      super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      cv.visit(version, access, name, signature, superName, interfaces);
      className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
      if (className.startsWith(pattern)) {
        mv = new MethodInstrumentorMethodVisitor(mv, name + desc);
      }
      return mv;
    }
  }

  static class MethodInstrumentorMethodVisitor extends MethodVisitor implements Opcodes {
    private String methodName;

    public MethodInstrumentorMethodVisitor(MethodVisitor mv, String name) {
      super(Opcodes.ASM5, mv);
      methodName = name;
    }

    @Override
    public void visitCode() {
      mv.visitCode();
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitLdcInsn("-> " + methodName);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    @Override
    public void visitInsn(int opcode) {
      if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("<- " + methodName);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
      }
      mv.visitInsn(opcode);
    }
  }
}
