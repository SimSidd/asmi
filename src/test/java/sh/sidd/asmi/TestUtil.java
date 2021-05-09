package sh.sidd.asmi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import sh.sidd.asmi.compiler.Compiler;
import sh.sidd.asmi.parser.Parser;
import sh.sidd.asmi.scanner.Scanner;

public final class TestUtil {

  private TestUtil() {}

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
  public static ErrorHandler compileSource(String source) {
    final var errorHandler = new ErrorHandler();
    final var scanner = new Scanner(source, errorHandler);
    final var tokens = scanner.scanTokens();
    final var parser = new Parser(errorHandler, tokens);
    final var ast = parser.parse();
    final var compiler = new Compiler(errorHandler, ast, scanner);

    compiler.compile();

    return errorHandler;
  }

  /**
   * Asserts that a specific error has occurred.
   *
   * @param message The message to look for.
   * @param errorHandler The error handler to check.
   */
  public static void assertError(String message, ErrorHandler errorHandler) {
    assertThat(errorHandler.hasErrors()).isTrue();

    final var allErrors = String.join("\n", errorHandler.getErrors());
    assertThat(allErrors).contains(message);
  }
}
