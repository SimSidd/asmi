package sh.sidd.asmi.parser;

import java.util.List;
import sh.sidd.asmi.ErrorHandler;
import sh.sidd.asmi.data.Expr;
import sh.sidd.asmi.data.Token;
import sh.sidd.asmi.data.TokenType;

/** Parses a list of tokens into an abstract syntax tree. */
public class Parser {

  private final ErrorHandler errorHandler;
  private final TokenReader reader;

  public Parser(ErrorHandler errorHandler, List<Token> tokens) {
    this.errorHandler = errorHandler;
    this.reader = new TokenReader(tokens);
  }

  /**
   * Parses the given list of tokens into an expression.
   *
   * @return The parsed expression.
   */
  public Expr parse() {
    try {
      return parseExpression();
    } catch (ParserException ex) {
      errorHandler.report(ex.getToken(), ex.getMessage());
      return null;
    }
  }

  /** Parses a single expression. */
  private Expr parseExpression() {
    return parseEquality();
  }

  /** Parses a single equality-expression. */
  private Expr parseEquality() {
    var expr = parseComparison();

    while (reader.advanceIfMatch(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
      final var operator = reader.previous();
      final var right = parseComparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr parseComparison() {
    var expr = parseTerm();

    while (reader.advanceIfMatch(
        TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      final var operator = reader.previous();
      final var right = parseTerm();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr parseTerm() {
    var expr = parseFactor();

    while (reader.advanceIfMatch(TokenType.MINUS, TokenType.PLUS)) {
      final var operator = reader.previous();
      final var right = parseFactor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr parseFactor() {
    Expr expr = parseUnary();

    while (reader.advanceIfMatch(TokenType.SLASH, TokenType.STAR)) {
      final var operator = reader.previous();
      final var right = parseUnary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr parseUnary() {
    if (reader.advanceIfMatch(TokenType.BANG, TokenType.MINUS)) {
      final var operator = reader.previous();
      final var right = parseUnary();
      return new Expr.Unary(operator, right);
    }

    return parsePrimary();
  }

  /** Parses a single primary-expression. */
  private Expr parsePrimary() {
    if (reader.advanceIfMatch(TokenType.FALSE)) {
      return new Expr.Literal(false);
    }

    if (reader.advanceIfMatch(TokenType.TRUE)) {
      return new Expr.Literal(true);
    }

    if (reader.advanceIfMatch(TokenType.NULL)) {
      return new Expr.Literal(null);
    }

    if (reader.advanceIfMatch(TokenType.NUMBER, TokenType.STRING)) {
      return new Expr.Literal(reader.previous().literal());
    }

    if (reader.advanceIfMatch(TokenType.LEFT_PAREN)) {
      final var expr = parseExpression();
      reader.consumeExpected(TokenType.RIGHT_PAREN, "Expected ')' after expression.");
      return new Expr.Grouping(expr);
    }

    throw new ParserException(reader.peek(), "Expected expression.");
  }
}
