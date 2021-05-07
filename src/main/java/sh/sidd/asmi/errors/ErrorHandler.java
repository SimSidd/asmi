package sh.sidd.asmi.errors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
}
