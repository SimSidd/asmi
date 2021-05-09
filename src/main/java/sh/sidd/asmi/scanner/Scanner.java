package sh.sidd.asmi.scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import sh.sidd.asmi.ErrorHandler;
import sh.sidd.asmi.data.Token;
import sh.sidd.asmi.data.TokenType;

/** Scans Asmi source code and generates a list of tokens. */
public class Scanner implements SourceRetriever {
  private final String source;
  private final List<String> sourceLines;
  private final SourceReader reader;
  private final List<Token> tokens = new ArrayList<>();
  private final Map<String, TokenType> keywordTokens;
  private final ErrorHandler errorHandler;
  private int tokenStartPos;
  private int currentLine;

  public Scanner(String source, ErrorHandler errorHandler) {
    this.reader = new SourceReader(source);
    this.source = source;
    this.sourceLines = Arrays.asList(source.split("\\n"));
    this.errorHandler = errorHandler;
    this.keywordTokens = TokenType.getKeywordTokens();
  }

  /**
   * Scans and returns all tokens of the file.
   */
  public List<Token> scanTokens() {
    while (!reader.isAtEnd()) {
      tokenStartPos = reader.getCurrentPos();
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, currentLine));

    return tokens;
  }

  /**
   * Scans a single token.
   */
  private void scanToken() {
    final var c = reader.readAndAdvance();

    switch(c) {
      case '(' -> addToken(TokenType.LEFT_PAREN);
      case ')' -> addToken(TokenType.RIGHT_PAREN);
      case '{' -> addToken(TokenType.LEFT_BRACE);
      case '}' -> addToken(TokenType.RIGHT_BRACE);
      case ',' -> addToken(TokenType.COMMA);
      case '.' -> addToken(TokenType.DOT);
      case '-' -> addToken(TokenType.MINUS);
      case '+' -> addToken(TokenType.PLUS);
      case '*' -> addToken(TokenType.STAR);
      case '/' -> addToken(TokenType.SLASH);
      case '!' -> addToken(reader.advanceIfMatch('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
      case '=' -> addToken(reader.advanceIfMatch('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
      case '<' -> addToken(reader.advanceIfMatch('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
      case '>' -> addToken(reader.advanceIfMatch('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);

      case ' ', '\r', '\t' -> {}

      case '\n' -> currentLine++;

      case '"' -> scanString();

      case '#' -> scanLineComment();

      default -> {
        if(isDigit(c)) {
          scanNumber();
        } else if(isAlpha(c)) {
          scanIdentifier();
        } else {
          errorHandler.report(currentLine, "Unexpected character: " + c);
        }
      }
    }
  }

  /**
   * Adds a single token of the given type.
   *
   * @param tokenType The type of the token to add.
   */
  private void addToken(TokenType tokenType) {
    addToken(tokenType, null);
  }

  /**
   * Adds a single token with a given literal.
   *
   * @param tokenType The type of the token to add.
   * @param literal The literal to set for the token.
   */
  private void addToken(TokenType tokenType, Object literal) {
    final var text = source.substring(tokenStartPos, reader.getCurrentPos());
    tokens.add(new Token(tokenType, text, literal, currentLine));
  }

  /**
   * Scans a single string token.
   */
  private void scanString() {
    while(reader.peek() != '"' && !reader.isAtEnd()) {
      if(reader.peek() == '\n') {
        currentLine++;
      }

      reader.readAndAdvance();
    }

    if(reader.isAtEnd()) {
      errorHandler.report(currentLine, "Unterminated string.");
      return;
    }

    reader.readAndAdvance();

    final var value = source.substring(tokenStartPos + 1, reader.getCurrentPos() - 1);

    addToken(TokenType.STRING, value);
  }

  /**
   * Scans a line comment until the end of the line.
   *
   * Does not create any tokens.
   */
  private void scanLineComment() {
    while(reader.peek() != '\n' && !reader.isAtEnd()) {
      reader.readAndAdvance();
    }
  }

  /**
   * Scans a single number token.
   */
  private void scanNumber() {
    var isFloating = false;

    while(isDigit(reader.peek())) {
      reader.readAndAdvance();
    }

    if(reader.peek() == '.' && isDigit(reader.peekNext())) {
      reader.readAndAdvance();
      isFloating = true;

      while(isDigit(reader.peek())) {
        reader.readAndAdvance();
      }
    }

    if(isFloating) {
      addToken(TokenType.NUMBER, Double.parseDouble(source.substring(tokenStartPos, reader.getCurrentPos())));
    } else {
      addToken(TokenType.NUMBER, Integer.parseInt(source.substring(tokenStartPos, reader.getCurrentPos())));
    }
  }

  /**
   * Scans a single identifier token.
   */
  private void scanIdentifier() {
    while(isAlphaNumeric(reader.peek())) {
      reader.readAndAdvance();
    }

    final var text = source.substring(tokenStartPos, reader.getCurrentPos());
    var tokenType = keywordTokens.get(text);

    if(tokenType == null) {
      tokenType = TokenType.IDENTIFIER;
    }

    addToken(tokenType);
  }

  /**
   * Checks whether the given character is [0-9].
   *
   * @param c The character to check.
   * @return {@code true} If the given character is a digit.
   */
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  /**
   * Checks whether the given character is [a-zA-Z_].
   *
   * @param c The character to check.
   * @return {@code true} If the given character was alphabetical.
   */
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '_';
  }

  /**
   * Checks whether the given character is either {@link #isAlpha} or {@link #isDigit}
   *
   * @param c The character to check.
   * @return {@code true} If the character was alphanumeric.
   */
  private boolean isAlphaNumeric(char c) {
     return isAlpha(c) || isDigit(c);
  }

  @Override
  public String getLine(int line) {
    if(line >= sourceLines.size()) {
      throw new ScannerException(String.format("Cannot retrieve line at %d", line));
    }

    return sourceLines.get(line);
  }
}
