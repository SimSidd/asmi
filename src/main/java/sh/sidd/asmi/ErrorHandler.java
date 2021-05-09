package sh.sidd.asmi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import sh.sidd.asmi.data.Token;

/** Collects and reports errors which occurred during the different stages. */
@Slf4j
public class ErrorHandler {
  private final List<String> errors;

  public ErrorHandler() {
    errors = new ArrayList<>();
  }

  /** Checks whether any errors occurred. */
  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  /** Checks whether any errors occurred. */
  public List<String> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  /**
   * Reports a single error.
   *
   * @param line The line at which the error occurred.
   * @param message The error message.
   */
  public void report(int line, String message) {
    logError(String.format("[line %d] Error: %s", line + 1, message));
  }

  /**
   * Reports a single error based on a token.
   *
   * @param token The token which caused the error.
   * @param message The error message.
   */
  public void report(Token token, String message) {
    logError(
        String.format("[line %d] Error at '%s': %s", token.line() + 1, token.lexeme(), message));
  }

  /**
   * Logs a new error.
   *
   * @param message The message of the error.
   */
  private void logError(String message) {
    log.error(message);
    errors.add(message);
  }
}
