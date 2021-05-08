package sh.sidd.asmi.parser;

import java.util.List;
import sh.sidd.asmi.data.Token;
import sh.sidd.asmi.data.TokenType;

/** Reader class to retrieve tokens from a list of tokens. */
public class TokenReader {
  private final List<Token> tokens;
  private int currentPos;

  public TokenReader(List<Token> tokens) {
    this.tokens = tokens;
  }

  /**
   * Reads a single token and advances the current position if the token type matches.
   *
   * @param types The expected token types to match.
   * @return {@code true} If the token matched and the position was advanced.
   */
  public boolean advanceIfMatch(TokenType... types) {
    for (final var type : types) {
      if (check(type)) {
        readAndAdvance();
        return true;
      }
    }

    return false;
  }

  /**
   * Checks if the token at the current position has the given type.
   *
   * @param type The token type to check for.
   * @return {@code true} If the current token matched.
   */
  public boolean check(TokenType type) {
    if (isAtEnd()) {
      return false;
    }

    return peek().tokenType() == type;
  }

  /** Checks whether the current position is at the end of the token stream. */
  public boolean isAtEnd() {
    return peek().tokenType() == TokenType.EOF;
  }

  /** Reads a single token and advances the current position by one token. */
  public Token readAndAdvance() {
    if (!isAtEnd()) {
      currentPos++;
    }

    return previous();
  }

  /** Reads the current token without advancing the current position. */
  public Token peek() {
    return tokens.get(currentPos);
  }

  /** Reads the previous token without advancing the current position. */
  public Token previous() {
    return tokens.get(currentPos - 1);
  }

  /**
   * Advances the current position if the current token matches the given type.
   *
   * @param expected The expected token type.
   * @param message The error message if the type did not match.
   * @throws ParserException If the token type did not match.
   */
  public void consumeExpected(TokenType expected, String message) {
    if (check(expected)) {
      readAndAdvance();
    }

    throw new ParserException(peek(), message);
  }
}
