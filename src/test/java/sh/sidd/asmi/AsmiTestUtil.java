package sh.sidd.asmi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

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

    assertThat(errorHandler.hasErrors()).isTrue();

    final var allErrors = String.join("\n", errorHandler.getErrors());
    assertThat(allErrors).contains(message);
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
   * Compiles and runs the given source file content.
   *
   * <p>Assumes that the code does not have any parse or compile errors.
   *
   * @param source Source code to run.
   */
  public static ErrorHandler runSource(String source) throws Throwable {
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
