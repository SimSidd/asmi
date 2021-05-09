package sh.sidd.asmi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class EndToEndTests {

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
   * <p>Source files can use the `assert` statement to test their code.
   *
   * <pre>
   *   assert 1 + 1 == 2
   * </pre>
   *
   * @param path Path to the source.
   */
  private void runEndToEnd(Path path) throws Throwable {
    final var source = Files.readString(path);

    TestUtil.runSource(source);
  }
}
