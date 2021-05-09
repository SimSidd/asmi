package sh.sidd.asmi.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

/**
 * All possible tokens of the Asmi language. Tokens may optionally define keywords which represent
 * them.
 */
public enum TokenType {
  // Single-character tokens.
  LEFT_PAREN,
  RIGHT_PAREN,
  LEFT_BRACE,
  RIGHT_BRACE,
  COMMA,
  DOT,
  MINUS,
  PLUS,
  SLASH,
  STAR,
  HASH,

  // One or wto character tokens.
  BANG,
  BANG_EQUAL,
  EQUAL,
  EQUAL_EQUAL,
  GREATER,
  GREATER_EQUAL,
  LESS,
  LESS_EQUAL,

  // Literals.
  IDENTIFIER,
  STRING,
  NUMBER,

  // Keywords.
  AND("and"),
  OR("or"),
  IF("if"),
  ELSE("else"),
  TRUE("true"),
  FALSE("false"),
  DEF("def"),
  END("end"),
  FOR("for"),
  NULL("null"),
  PRINT("print"),
  RETURN("return"),
  THIS("this"),
  VAR("var"),
  WHILE("while"),
  CLASS("class"),
  ASSERT("assert"),

  EOF;

  @Getter private final String keyword;

  TokenType() {
    this.keyword = null;
  }

  TokenType(String keyword) {
    this.keyword = keyword;
  }

  /** Checks whether this token is a keyword token. */
  public boolean isKeyword() {
    return keyword != null;
  }

  /** Collects and returns all keyword tokens. */
  public static Map<String, TokenType> getKeywordTokens() {
    final var keywordTokens = new HashMap<String, TokenType>();

    for (final var token : TokenType.values()) {
      if (token.isKeyword()) {
        keywordTokens.put(token.getKeyword(), token);
      }
    }

    return Collections.unmodifiableMap(keywordTokens);
  }
}
