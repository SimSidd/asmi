package sh.sidd.asmi.scanner;

import lombok.Getter;

public class SourceReader {
  private final String source;
  @Getter private int currentPos;

  public SourceReader(String source) {
    this.source = source;
  }

  /** Checks whether the current position is at the end of the file. */
  public boolean isAtEnd() {
    return currentPos >= source.length();
  }

  /** Reads a single character and advances the current position by one character. */
  public char readAndAdvance() {
    return source.charAt(currentPos++);
  }

  /**
   * Reads a single character and advances the current position if the character matches.
   *
   * @param check The character to check against.
   * @return {@code true} If the character matched and the position was advanced.
   */
  public boolean advanceIfMatch(char check) {
    if (isAtEnd() || source.charAt(currentPos) != check) {
      return false;
    }

    currentPos++;

    return true;
  }

  /** Returns the character at the current position without advancing the position. */
  public char peek() {
    if (isAtEnd()) {
      return '\0';
    }

    return source.charAt(currentPos);
  }

  /** Returns the character at the next position without advancing the position. */
  public char peekNext() {
    if (currentPos + 1 >= source.length()) {
      return '\0';
    }

    return source.charAt(currentPos + 1);
  }
}
