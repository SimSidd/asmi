package sh.sidd.asmi.scanner;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.sidd.asmi.data.Token;
import sh.sidd.asmi.data.TokenType;
import sh.sidd.asmi.errors.ErrorHandler;

class ScannerTest {
  private ErrorHandler errorHandler;

  @BeforeEach
  void beforeEach() {
    errorHandler = new ErrorHandler();
  }

  @Test
  void shouldNotScanLineComments() {
    assertTokens("# comment", List.of());
    assertTokens("1 # comment", List.of(new Token(TokenType.NUMBER, "1", 1, 0)));
    assertTokens("# Line one\n1 # comment", List.of(new Token(TokenType.NUMBER, "1", 1, 1)));
  }

  /**
   * Asserts that the given source produces the expected tokens.
   *
   * <p>The EOF token will be added to the expected tokens.
   *
   * @param source The source to scan.
   * @param expectedTokens The expected tokens.
   */
  private void assertTokens(String source, List<Token> expectedTokens) {
    var scanner = new Scanner(source, errorHandler);
    var expected = new ArrayList<>(expectedTokens);

    expected.add(new Token(TokenType.EOF, "", null, StringUtils.countMatches(source, "\n")));

    assertEquals(expected, scanner.scanTokens());

    assertFalse(errorHandler.isHasError(), "Should not report any errors.");
  }
}
