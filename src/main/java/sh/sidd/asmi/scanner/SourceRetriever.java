package sh.sidd.asmi.scanner;

/** Interface to retrieve parts of the original source. */
public interface SourceRetriever {

  /**
   * Returns the given line of the source.
   *
   * @param line The 0-indexed line number to retrieve.
   * @return The line in the source.
   */
  String getLine(int line);

  /**
   * Returns the given lines of the source.
   *
   * @param lineStart The 0-indexed start of the lines to retrieve.
   * @param lineEnd The 0-indexed end of the lines to retrieve.
   * @return The lines in the source.
   */
  default String getLines(int lineStart, int lineEnd) {
    if (lineEnd < lineStart) {
      throw new ScannerException(
          String.format("Cannot retrieve source lines from %d to %d", lineStart, lineEnd));
    }

    final var sb = new StringBuilder();

    for (int i = lineStart; i <= lineEnd; i++) {
      sb.append(getLine(i));
    }

    return sb.toString();
  }
}
