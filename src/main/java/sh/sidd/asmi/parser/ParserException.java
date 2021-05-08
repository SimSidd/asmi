package sh.sidd.asmi.parser;

import lombok.Getter;
import sh.sidd.asmi.data.Token;

public class ParserException extends RuntimeException {
  @Getter private final Token token;

  public ParserException(Token token, String message) {
    super(message);
    this.token = token;
  }
}
