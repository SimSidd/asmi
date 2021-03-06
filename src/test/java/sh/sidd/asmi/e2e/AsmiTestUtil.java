package sh.sidd.asmi.e2e;

import org.assertj.core.api.Assertions;
import sh.sidd.asmi.ErrorHandler;
import sh.sidd.asmi.compiler.Compiler;
import sh.sidd.asmi.parser.Parser;
import sh.sidd.asmi.scanner.Scanner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.*;

public final class AsmiTestUtil {

  private record SourceOutput(String output, String bytecode, ErrorHandler errorHandler) {}

  private AsmiTestUtil() {}

  /**
   * Asserts that a specific compile error occurs.
   *
   * @param source The source to compile .
   * @param message The message to look for.
   */
  public static void assertCompileError(String source, String message) {
    final var errorHandler = compileSource(source).errorHandler;

    Assertions.assertThat(errorHandler.hasErrors()).isTrue();

    final var allErrors = String.join("\n", errorHandler.getErrors());
    assertThat(allErrors).contains(message);

    // TODO Assert that there is only one error.
  }

  /**
   * Asserts that given source code runs without any errors.
   *
   * @param source The source to run.
   */
  public static void assertNoErrors(String source) {
    assertThatCode(() -> runSource(source)).doesNotThrowAnyException();
  }

  /**
   * Asserts that given source code produces a runtime error.
   *
   * @param source The source to run.
   * @param message Part of the expected error message.
   */
  public static void assertRuntimeError(String source, String message) {
    assertThatThrownBy(() -> runSource(source))
        .isInstanceOf(Throwable.class)
        .hasMessageContaining(message);
  }

  /**
   * Asserts that given source code produces a runtime error.
   *
   * @param source The source to run.
   */
  public static void assertRuntimeError(String source) {
    assertThatThrownBy(() -> runSource(source))
        .isInstanceOf(Throwable.class);
  }

  /**
   * Asserts that the given code produces the expected output.
   *
   * @param source The source to run.
   * @param expectedOutput The expected output.
   */
  public static void assertOutput(String source, String expectedOutput) {
    SourceOutput output = null;

    //noinspection CatchMayIgnoreException
    try {
      output = runSource(source);
    } catch (Throwable ex) { // NOPMD
      Assertions.fail("Failed to run program", ex);
    }

    if (output == null) {
      Assertions.fail("No output");
    } else {
      try {
      assertThat(output.output.replaceAll("\\r", "").strip())
          .isEqualTo(expectedOutput.replaceAll("\\r", "").strip());
      } catch (AssertionError ex) {
        System.out.println(output.bytecode);
        throw ex;
      }
    }
  }

  /**
   * Runs the given source and captures its output.
   *
   * @param source Source code to run.
   * @return The output of the program.
   */
  private static SourceOutput runSource(String source) throws Throwable {
    final var errorHandler = new ErrorHandler();
    final var scanner = new Scanner(source, errorHandler);
    final var tokens = scanner.scanTokens();
    final var parser = new Parser(errorHandler, tokens);
    final var ast = parser.parse();
    final var compiler = new Compiler(errorHandler, ast, scanner);

    assertThat(errorHandler.hasErrors()).as("Should not have parse errors.").isFalse();

    compiler.compile();

    assertThat(errorHandler.hasErrors()).as("Should not have compile errors.").isFalse();

    final var originalOut = System.out;
    final var outBaos = new ByteArrayOutputStream();

    System.setOut(new PrintStream(outBaos));
    compiler.run();
    System.setOut(originalOut);

    return new SourceOutput(outBaos.toString(), compiler.getByteCode(), errorHandler);
  }

  /**
   * Compiles the given source.
   *
   * @param source The source to compile.
   * @return The error handler used during parsing and compilation.
   */
  private static SourceOutput compileSource(String source) {
    final var errorHandler = new ErrorHandler();
    final var scanner = new Scanner(source, errorHandler);
    final var tokens = scanner.scanTokens();
    final var parser = new Parser(errorHandler, tokens);
    final var ast = parser.parse();
    final var compiler = new Compiler(errorHandler, ast, scanner);

    compiler.compile();

    return new SourceOutput(null, null, errorHandler);
  }
}
