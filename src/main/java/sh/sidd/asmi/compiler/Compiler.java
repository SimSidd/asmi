package sh.sidd.asmi.compiler;

import java.io.PrintWriter;
import java.io.StringWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import sh.sidd.asmi.data.Expr;
import sh.sidd.asmi.data.Expr.Binary;
import sh.sidd.asmi.data.Expr.Grouping;
import sh.sidd.asmi.data.Expr.Literal;
import sh.sidd.asmi.data.Expr.Unary;

@Slf4j
public class Compiler implements Expr.Visitor<Object> {

  public class AsmiClassLoader extends ClassLoader {
    public Class defineClass(String name, byte[] b) {
      return defineClass(name, b, 0, b.length);
    }
  }

  private final ClassWriter classWriter;
  private final StringWriter stringWriter;
  private final CheckClassAdapter checkClassAdapter;
  private final TraceClassVisitor traceClassVisitor;
  private final ClassVisitor classVisitor;

  public Compiler() {
    classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    stringWriter = new StringWriter();
    traceClassVisitor = new TraceClassVisitor(classWriter, new PrintWriter(stringWriter));
    checkClassAdapter = new CheckClassAdapter(traceClassVisitor);
    classVisitor = checkClassAdapter;

    byteCodeTest();
  }

  private void byteCodeTest() {
    classVisitor.visit(
        Opcodes.V16,
        Opcodes.ACC_PUBLIC,
        "sh/sidd/asmi/Compiled",
        null,
        "java/lang/Object",
        new String[] {});

    final var constructor =
        classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
    constructor.visitVarInsn(Opcodes.ALOAD, 0);
    constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    constructor.visitInsn(Opcodes.RETURN);
    constructor.visitMaxs(0, 0);
    constructor.visitEnd();

    final var methodVisitor =
        classVisitor.visitMethod(Opcodes.ACC_PUBLIC, "print", "(Ljava/lang/Object;)V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitFieldInsn(
        Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
    methodVisitor.visitMethodInsn(
        Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);

    methodVisitor.visitInsn(Opcodes.RETURN);
    methodVisitor.visitMaxs(2, 2);
    methodVisitor.visitEnd();

    classVisitor.visitEnd();

    System.out.println(stringWriter);

    try {
      final var compiledClass =
          new AsmiClassLoader().defineClass("sh.sidd.asmi.Compiled", classWriter.toByteArray());
      final var instance = compiledClass.getDeclaredConstructor().newInstance();

      MethodUtils.invokeMethod(instance, "print", "hello bytecode");
    } catch (Exception ex) {
      log.error("Class creation failed", ex);
    }
  }

  /** Finishes visiting and writes the compiled class to its destination. */
  public void visitEnd() {}

  @Override
  public Object visitBinaryExpr(Binary expr) {
    return null;
  }

  @Override
  public Object visitGroupingExpr(Grouping expr) {
    return null;
  }

  @Override
  public Object visitLiteralExpr(Literal expr) {
    return null;
  }

  @Override
  public Object visitUnaryExpr(Unary expr) {
    return null;
  }
}
