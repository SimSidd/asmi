package sh.sidd.asmi.parser;

import java.util.List;
import sh.sidd.asmi.data.Token;
import sh.sidd.asmi.data.TokenType;

public class TokenReader {
  private final List<Token> tokens;
  private int currentPos;

  public TokenReader(List<Token> tokens) {
    this.tokens = tokens;
  }

  public boolean advanceIfMatch(TokenType... types) {
    for (final var type : types) {
      if (check(type)) {
        readAndAdvance();
        return true;
      }
    }

    return false;
  }

  public boolean check(TokenType type) {
    if (isAtEnd()) {
      return false;
    }

    return peek().tokenType() == type;
  }

  public boolean isAtEnd() {
    return peek().tokenType() == TokenType.EOF;
  }

  public Token readAndAdvance() {
    if (!isAtEnd()) {
      currentPos++;
    }

    return previous();
  }

  public Token peek() {
    return tokens.get(currentPos);
  }

  public Token previous() {
    return tokens.get(currentPos - 1);
  }

  public void consumeExpected(TokenType expected, String message) {
    if (check(expected)) {
      readAndAdvance();
    }

    throw new ParserException(peek(), message);
  }
}
