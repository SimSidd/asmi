package sh.sidd.asmi.compiler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;
import sh.sidd.asmi.data.ValueType;

/** Writer to write bytecode for a single .class file. */
@Slf4j
public class ByteCodeWriter {
  private final ClassWriter classWriter;
  private final StringWriter traceStringWriter;
  private final TraceClassVisitor traceClassVisitor;
  private final ClassVisitor classVisitor;
  private MethodVisitor methodVisitor;

  private Label methodStart;
  private Label methodEnd;

  private static class AsmiClassLoader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] b) {
      return defineClass(name, b, 0, b.length);
    }
  }

  public ByteCodeWriter() {
    classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    traceStringWriter = new StringWriter();
    traceClassVisitor = new TraceClassVisitor(classWriter, new PrintWriter(traceStringWriter));
    classVisitor = traceClassVisitor;
    methodVisitor = null;
  }

  public void startClass() {
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
  }

  public void finishClass() {
    classVisitor.visitEnd();
  }

  public void run() throws Throwable {
    final var compiledClass =
        new AsmiClassLoader().defineClass("sh.sidd.asmi.Compiled", classWriter.toByteArray());
    final var instance = compiledClass.getDeclaredConstructor().newInstance();

    try {
      MethodUtils.invokeMethod(instance, "execute");
    } catch (InvocationTargetException ex) {
      throw ex.getTargetException();
    }
  }

  public void startMethod() {
    methodVisitor = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, "execute", "()V", null, null);
    methodVisitor.visitCode();

    methodStart = new Label();
    methodEnd = new Label();

    methodVisitor.visitLabel(methodStart);
  }

  public void endMethod() {
    methodVisitor.visitLabel(methodEnd);

    methodVisitor.visitInsn(Opcodes.RETURN);

    // CheckClassAdapter does not work together with ClassWriter.COMPUTE_FRAMES and requires to have
    // large enough values. The values passed here will be ignored by the actual ClassWriter.
    methodVisitor.visitMaxs(Short.MAX_VALUE, Short.MAX_VALUE);

    methodVisitor.visitEnd();
  }

  /**
   * Pushes a constant value on to the local variables stack;
   *
   * @param value The value to push.
   */
  public void writeConstant(Object value) {
    methodVisitor.visitLdcInsn(value);
  }

  /**
   * Writes the opcode for multiplication of the given type.
   *
   * @param valueType The type of the current value.
   */
  public void writeMul(ValueType valueType) throws ByteCodeException {
    final var opcode = switch(valueType) {
      case SHORT, INT -> Opcodes.IMUL;
      case LONG -> Opcodes.LMUL;
      case FLOAT -> Opcodes.FMUL;
      case DOUBLE -> Opcodes.DMUL;
      default -> throw new ByteCodeException("Cannot multiply given type.");
    };

    methodVisitor.visitInsn(opcode);
  }

  /**
   * Writes the opcode for integer addition.
   *
   * @param valueType The type of the current value.
   */
  public void writeAdd(ValueType valueType) throws ByteCodeException {
    final var opcode = switch(valueType) {
      case SHORT, INT -> Opcodes.IADD;
      case LONG -> Opcodes.LADD;
      case FLOAT -> Opcodes.FADD;
      case DOUBLE -> Opcodes.DADD;
      default -> throw new ByteCodeException("Cannot add given type.");
    };

    methodVisitor.visitInsn(opcode);
  }

  /**
   * Writes the opcode for integer subtraction.
   *
   * @param valueType The type of the current value.
   */
  public void writeSub(ValueType valueType) throws ByteCodeException {
    final var opcode = switch(valueType) {
      case SHORT, INT -> Opcodes.ISUB;
      case LONG -> Opcodes.LSUB;
      case FLOAT -> Opcodes.FSUB;
      case DOUBLE -> Opcodes.DSUB;
      default -> throw new ByteCodeException("Cannot subtract given type.");
    };

    methodVisitor.visitInsn(opcode);
  }

  /**
   * Writes the opcode for integer division.
   *
   * @param valueType The type of the current value.
   */
  public void writeDiv(ValueType valueType) throws ByteCodeException {
    final var opcode = switch(valueType) {
      case SHORT, INT -> Opcodes.IDIV;
      case LONG -> Opcodes.LDIV;
      case FLOAT -> Opcodes.FDIV;
      case DOUBLE -> Opcodes.DDIV;
      default -> throw new ByteCodeException("Cannot divide given type.");
    };

    methodVisitor.visitInsn(opcode);
  }

  /**
   * Writes a cast from one type to another.
   *
   * @param originalType The original type of the value.
   * @param targetType The new type of the value.
   */
  public void writeCast(ValueType originalType, ValueType targetType) throws ByteCodeException {
    final var opcode = switch(originalType) {
      case SHORT, INT -> switch (targetType) {
        case LONG -> Opcodes.I2L;
        case FLOAT -> Opcodes.I2F;
        case DOUBLE -> Opcodes.I2D;
        default -> null;
      };

      case LONG -> switch(targetType) {
        case SHORT, INT -> Opcodes.L2I;
        case FLOAT -> Opcodes.L2F;
        case DOUBLE -> Opcodes.L2D;
        default -> null;
      };

      case FLOAT -> switch (targetType) {
        case SHORT, INT -> Opcodes.F2I;
        case LONG -> Opcodes.F2L;
        case DOUBLE -> Opcodes.F2D;
        default -> null;
      };

      case DOUBLE -> switch (targetType) {
        case SHORT, INT -> Opcodes.D2I;
        case LONG -> Opcodes.D2L;
        case FLOAT -> Opcodes.D2F;
        default -> null;
      };

      default -> null;
    };

    if(opcode == null) {
      throw new ByteCodeException("Could not determine cast types.");
    }

    methodVisitor.visitInsn(opcode);
  }

  /**
   * Writes call to System.out.println.
   *
   * @param valueType The type which the current value has.
   * @param setValue A runnable which puts the value to print on the stack.
   */
  public void writePrint(ValueType valueType, Runnable setValue) {
    final var descriptor = "(" + valueType.toDescriptor() +")V";

    methodVisitor.visitFieldInsn(
        Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

    setValue.run();

    methodVisitor.visitMethodInsn(
        Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", descriptor, false);
  }

  /**
   * Writes the bytecode for to an `assert` statement.
   *
   * `assert` checks if the current value on the stack equals 0 and throws {@link AssertionError}
   * if it is.
   *
   * @param message The message for the {@link AssertionError}.
   */
  public void writeAssert(String message) {
    final var continuation = new Label();

    methodVisitor.visitJumpInsn(Opcodes.IFNE, continuation);

    methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/AssertionError");
    methodVisitor.visitInsn(Opcodes.DUP);

    methodVisitor.visitLdcInsn(message);

    methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/AssertionError",
        "<init>", "(Ljava/lang/Object;)V", false);
    methodVisitor.visitInsn(Opcodes.ATHROW);

    methodVisitor.visitLabel(continuation);
  }

  /**
   * Writes *CMP for the given types.
   *
   * Pushes `1` onto the stack if the values are equal, otherwise '0'.
   *
   * @param valueType The type of the current values.
   */
  public void writeCmp(ValueType valueType) throws ByteCodeException {
    if(!valueType.isNumeric()) {
      throw new ByteCodeException("Cannot compare non-numeric types.");
    }

    final var continuationLabel = new Label();
    final var equalLabel = new Label();

    if(valueType == ValueType.FLOAT) {
      methodVisitor.visitInsn(Opcodes.FCMPG);
      methodVisitor.visitJumpInsn(Opcodes.IFEQ, equalLabel);
    } else if(valueType == ValueType.DOUBLE) {
      methodVisitor.visitInsn(Opcodes.DCMPG);
      methodVisitor.visitJumpInsn(Opcodes.IFEQ, equalLabel);
    } else if(valueType == ValueType.LONG) {
      methodVisitor.visitInsn(Opcodes.LCMP);
      methodVisitor.visitJumpInsn(Opcodes.IFEQ, equalLabel);
    } else {
      methodVisitor.visitJumpInsn(Opcodes.IF_ICMPEQ, equalLabel);
    }

    methodVisitor.visitInsn(Opcodes.ICONST_0);
    methodVisitor.visitJumpInsn(Opcodes.GOTO, continuationLabel);

    methodVisitor.visitLabel(equalLabel);
    methodVisitor.visitInsn(Opcodes.ICONST_1);

    methodVisitor.visitLabel(continuationLabel);
  }

  /**
   * Writes *NEG for the given type.
   *
   * @param valueType The type of the current value.
   */
  public void writeNeg(ValueType valueType) throws ByteCodeException {
    final var opcode = switch(valueType) {
      case SHORT, INT -> Opcodes.INEG;
      case LONG -> Opcodes.LNEG;
      case FLOAT -> Opcodes.FNEG;
      case DOUBLE -> Opcodes.DNEG;
      default -> null;
    };

    if(opcode == null) {
      throw new ByteCodeException("Cannot negate non-numeric types.");
    }

    methodVisitor.visitInsn(opcode);
  }

  /**
   * Stores the current value on the stack as a variable.
   *
   * @param valueType The type of the variable.
   * @param index The index of the variable to store.
   */
  public void storeVariable(ValueType valueType, int index) {
    final var opcode = switch(valueType) {
      case SHORT, INT -> Opcodes.ISTORE;
      case FLOAT -> Opcodes.FSTORE;
      case DOUBLE -> Opcodes.DSTORE;
      default -> Opcodes.ASTORE;
    };

    methodVisitor.visitVarInsn(opcode, index);
  }

  /**
   * Loads the value of the given variable onto the stack.
   *
   * @param valueType The type of the variable to load.
   * @param index The index of the variable to load.
   */
  public void loadVariable(ValueType valueType, int index) {
    final var opcode = switch(valueType) {
      case SHORT, INT -> Opcodes.ILOAD;
      case FLOAT -> Opcodes.FLOAD;
      case DOUBLE -> Opcodes.DLOAD;
      default -> Opcodes.ALOAD;
    };

    methodVisitor.visitVarInsn(opcode, index);
  }

  /**
   * Writes the bytecode for an if-then-else block.
   *
   * @param setupCondition A runnable which pushes 1 onto the stack if the 'if' condition is true.
   *   If the condition is false 0 should be pushed to the stack.
   * @param thenBlock A runnable which executes the then-block.
   * @param elseBlock A runnable which executes the else-block.
   */
  public void writeIfThenElse(Runnable setupCondition, Runnable thenBlock, Runnable elseBlock) {
    final var continuationLabel = new Label();

    setupCondition.run();

    if(elseBlock == null) {
      methodVisitor.visitJumpInsn(Opcodes.IFEQ, continuationLabel);
      thenBlock.run();
    } else {
      final var elseLabel = new Label();

      methodVisitor.visitJumpInsn(Opcodes.IFEQ, elseLabel);
      thenBlock.run();
      methodVisitor.visitJumpInsn(Opcodes.GOTO, continuationLabel);
      methodVisitor.visitLabel(elseLabel);
      elseBlock.run();
    }

    methodVisitor.visitLabel(continuationLabel);
  }
}
