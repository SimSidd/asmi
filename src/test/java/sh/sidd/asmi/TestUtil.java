package sh.sidd.asmi;

import org.junit.jupiter.api.Assertions;
import sh.sidd.asmi.compiler.Compiler;
import sh.sidd.asmi.parser.Parser;
import sh.sidd.asmi.scanner.Scanner;

public class TestUtil {

  /**
   * Compiles and runs the given source file content.
   *
   * @param source Source code to run.
   */
  public static void runSource(String source) throws Throwable {
    final var errorHandler = new ErrorHandler();
    final var scanner = new Scanner(source, errorHandler);
    final var tokens = scanner.scanTokens();
    final var parser = new Parser(errorHandler, tokens);
    final var ast = parser.parse();
    final var compiler = new Compiler(errorHandler, ast, scanner);

    if (errorHandler.isHasError()) {
      Assertions.fail("Should not have parse errors.");
    }

    compiler.compile();

    if (errorHandler.isHasError()) {
      Assertions.fail("Should not have compile errors.");
    }

    compiler.run();
  }
}
