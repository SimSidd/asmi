package sh.sidd.asmi.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.assertj.core.api.Assertions;
import sh.sidd.asmi.ErrorHandler;
import sh.sidd.asmi.compiler.Compiler;
import sh.sidd.asmi.parser.Parser;
import sh.sidd.asmi.scanner.Scanner;

public final class AsmiTestUtil {

  private AsmiTestUtil() {}

  /**
   * Asserts that a specific compile error occurs.
   *
   * @param source The source to compile .
   * @param message The message to look for.
   */
  public static void assertCompileError(String source, String message) {
    final var errorHandler = compileSource(source);

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
   * Asserts that the given code produces the expected output.
   *
   * @param source The source to run.
   * @param expectedOutput The expected output.
   */
  public static void assertOutput(String source, String expectedOutput) {
    String output = "";

    //noinspection CatchMayIgnoreException
    try {
      output = runSourceWithOutput(source);
    } catch (Throwable ex) { // NOPMD
      Assertions.fail("Failed to run program", ex);
    }

    assertThat(output.replaceAll("\\r", "").strip())
        .isEqualTo(expectedOutput.replaceAll("\\r", "").strip());
  }

  /**
   * Compiles and runs the given source file content.
   *
   * <p>Assumes that the code does not have any parse or compile errors.
   *
   * @param source Source code to run.
   */
  private static ErrorHandler runSource(String source) throws Throwable {
    final var errorHandler = new ErrorHandler();
    final var scanner = new Scanner(source, errorHandler);
    final var tokens = scanner.scanTokens();
    final var parser = new Parser(errorHandler, tokens);
    final var ast = parser.parse();
    final var compiler = new Compiler(errorHandler, ast, scanner);

    assertThat(errorHandler.hasErrors()).as("Should not have parse errors.").isFalse();

    compiler.compile();

    assertThat(errorHandler.hasErrors()).as("Should not have compile errors.").isFalse();

    compiler.run();

    return errorHandler;
  }

  /**
   * Runs the given source and captures its output.
   *
   * @param source Source code to run.
   * @return The output of the program.
   */
  private static String runSourceWithOutput(String source) throws Throwable {
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
    return outBaos.toString();
  }

  /**
   * Compiles the given source.
   *
   * @param source The source to compile.
   * @return The error handler used during parsing and compilation.
   */
  private static ErrorHandler compileSource(String source) {
    final var errorHandler = new ErrorHandler();
    final var scanner = new Scanner(source, errorHandler);
    final var tokens = scanner.scanTokens();
    final var parser = new Parser(errorHandler, tokens);
    final var ast = parser.parse();
    final var compiler = new Compiler(errorHandler, ast, scanner);

    compiler.compile();

    return errorHandler;
  }
}
