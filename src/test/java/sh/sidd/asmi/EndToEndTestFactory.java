package sh.sidd.asmi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import sh.sidd.asmi.compiler.Compiler;
import sh.sidd.asmi.parser.Parser;
import sh.sidd.asmi.scanner.Scanner;

public class EndToEndTestFactory {

  /** Runs all end-to-end tests in the "e2e" directory. */
  @TestFactory
  public Stream<DynamicTest> endToEndTests() throws IOException {
    final var files =
        Files.find(
                Paths.get("src/test/java/sh/sidd/asmi/e2e"),
                Integer.MAX_VALUE,
                (path, basicFileAttributes) -> path.toString().endsWith(".asmi"))
            .toList();

    assertTrue(files.size() > 0, "Should have end-to-end tests");

    return files.stream()
        .map(p -> DynamicTest.dynamicTest(p.getFileName().toString(), () -> runEndToEnd(p)));
  }

  /**
   * Runs an end-to-end test for the given source file.
   *
   * <p>Each source file should have comments describing what the expected output of the program is:
   *
   * <pre>
   *   # Output: hello
   *   print "hello"
   * </pre>
   *
   * @param path Path to the source.
   */
  private void runEndToEnd(Path path) throws IOException {
    final var source = Files.readString(path);
    final var expectedOutput = getExpectedOutput(source);
    final var errorHandler = new ErrorHandler();
    final var scanner = new Scanner(source, errorHandler);
    final var tokens = scanner.scanTokens();
    final var parser = new Parser(errorHandler, tokens);
    final var ast = parser.parse();
    final var compiler = new Compiler(errorHandler, ast);

    assertFalse(expectedOutput.isBlank(), "Should have expected output.");

    compiler.compile();
    assertFalse(errorHandler.isHasError(), "Should not have errors.");

    final var outStream = new ByteArrayOutputStream();
    final var originalOut = System.out;
    String output;

    try {
      System.setOut(new PrintStream(outStream));

      compiler.run();

      output = outStream.toString().replaceAll("\\r", "").strip();
      assertEquals(expectedOutput, output);
    } finally {
      System.setOut(originalOut);
    }

    System.out.print(output);
  }

  /**
   * Extracts the expected program output from the given test source.
   *
   * @param testSource The source of the test.
   * @return The expected output of the test.
   */
  private String getExpectedOutput(String testSource) {
    return String.join(
        "\n",
        Arrays.stream(testSource.split("\\n"))
            .filter(s -> s.startsWith("# Output:"))
            .map(s -> s.replaceFirst("# Output: ", ""))
            .map(String::strip)
            .toList());
  }
}
