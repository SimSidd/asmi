package sh.sidd.asmi;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import sh.sidd.asmi.data.Token;

/** Collects and reports errors which occurred during the different stages. */
@Slf4j
public class ErrorHandler {
  @Getter private boolean hasError;

  /**
   * Reports a single error.
   *
   * @param line The line at which the error occurred.
   * @param message The error message.
   */
  public void report(int line, String message) {
    hasError = true;
    log.error(String.format("[line %d] Error: %s", line, message));
  }

  /**
   * Reports a single error based on a token.
   *
   * @param token The token which caused the error.
   * @param message The error message.
   */
  public void report(Token token, String message) {
    hasError = true;
    log.error(String.format("[line %d] Error at '%s': %s", token.line(), token.lexeme(), message));
  }
}
