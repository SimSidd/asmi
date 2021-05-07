package sh.sidd.asmi.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import sh.sidd.asmi.data.Token;
import sh.sidd.asmi.data.TokenType;
import sh.sidd.asmi.errors.ErrorHandler;

/** Scans Asmi source code and generates a list of tokens. */
public class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private final Map<String, TokenType> keywordTokens;
  private final ErrorHandler errorHandler;
  private int tokenStartPos;
  private int currentPos;
  private int currentLine;

  public Scanner(String source, ErrorHandler errorHandler) {
    this.source = source;
    this.errorHandler = errorHandler;
    this.keywordTokens = TokenType.getKeywordTokens();
  }

  /**
   * Scans and returns all tokens of the file.
   */
  public List<Token> scanTokens() {
    while (!isAtEnd()) {
      tokenStartPos = currentPos;
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, currentLine));

    return tokens;
  }

  /**
   * Scans a single token.
   */
  private void scanToken() {
    final var c = readAndAdvance();

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
      case '!' -> addToken(advanceIfMatch('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
      case '=' -> addToken(advanceIfMatch('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
      case '<' -> addToken(advanceIfMatch('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
      case '>' -> addToken(advanceIfMatch('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);

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
   * Checks whether the current position is at the end of the file.
   */
  private boolean isAtEnd() {
    return currentPos >= source.length();
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

  /**
   * Reads a single character and advances the current position by one character.
   */
  private char readAndAdvance() {
    return source.charAt(currentPos++);
  }

  /**
   * Reads a single character and advances the current position if the character matches.
   *
   * @param check The character to check against.
   * @return {@code true} If the character matched and the position was advanced.
   */
  private boolean advanceIfMatch(char check) {
    if(isAtEnd() || source.charAt(currentPos) != check) {
      return false;
    }

    currentPos++;

    return true;
  }

  /**
   * Returns the character at the current position without advancing the position.
   */
  private char peek() {
    if(isAtEnd()) {
      return '\0';
    }

    return source.charAt(currentPos);
  }

  /**
   * Returns the character at the next position without advancing the position.
   */
  private char peekNext() {
    if(currentPos + 1 >= source.length()) {
      return '\0';
    }

    return source.charAt(currentPos + 1);
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
    final var text = source.substring(tokenStartPos, currentPos);
    tokens.add(new Token(tokenType, text, literal, currentLine));
  }

  /**
   * Scans a single string token.
   */
  private void scanString() {
    while(peek() != '"' && !isAtEnd()) {
      if(peek() == '\n') {
        currentLine++;
      }

      readAndAdvance();
    }

    if(isAtEnd()) {
      errorHandler.report(currentLine, "Unterminated string.");
      return;
    }

    readAndAdvance();

    final var value = source.substring(tokenStartPos + 1, currentPos - 1);

    addToken(TokenType.STRING, value);
  }

  /**
   * Scans a line comment until the end of the line.
   *
   * Does not create any tokens.
   */
  private void scanLineComment() {
    while(peek() != '\n' && !isAtEnd()) {
      readAndAdvance();
    }
  }

  /**
   * Scans a single number token.
   */
  private void scanNumber() {
    var isFloating = false;

    while(isDigit(peek())) {
      readAndAdvance();
    }

    if(peek() == '.' && isDigit(peekNext())) {
      readAndAdvance();
      isFloating = true;

      while(isDigit(peek())) {
        readAndAdvance();
      }
    }

    if(isFloating) {
      addToken(TokenType.NUMBER, Double.parseDouble(source.substring(tokenStartPos, currentPos)));
    } else {
      addToken(TokenType.NUMBER, Integer.parseInt(source.substring(tokenStartPos, currentPos)));
    }
  }

  /**
   * Scans a single identifier token.
   */
  private void scanIdentifier() {
    while(isAlphaNumeric(peek())) {
      readAndAdvance();
    }

    var text = source.substring(tokenStartPos, currentPos);
    var tokenType = keywordTokens.get(text);

    if(tokenType == null) {
      tokenType = TokenType.IDENTIFIER;
    }

    addToken(tokenType);
  }
}
