package sh.sidd.asmi.parser;

import java.util.ArrayList;
import java.util.List;
import sh.sidd.asmi.ErrorHandler;
import sh.sidd.asmi.data.Expr;
import sh.sidd.asmi.data.Expr.BinaryExpr;
import sh.sidd.asmi.data.Expr.GroupingExpr;
import sh.sidd.asmi.data.Expr.LiteralExpr;
import sh.sidd.asmi.data.Expr.UnaryExpr;
import sh.sidd.asmi.data.Expr.VariableExpr;
import sh.sidd.asmi.data.Stmt;
import sh.sidd.asmi.data.Stmt.AssertStmt;
import sh.sidd.asmi.data.Stmt.ExpressionStmt;
import sh.sidd.asmi.data.Stmt.PrintStmt;
import sh.sidd.asmi.data.Stmt.VarStmt;
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
  public List<Stmt> parse() {
    final var statements = new ArrayList<Stmt>();

    try {
      while (!reader.isAtEnd()) {
        statements.add(parseStatement());
      }
    } catch (ParserException ex) {
      errorHandler.report(ex.getToken(), ex.getMessage());
      return null;
    }

    return statements;
  }

  /** Parses a single statement. */
  private Stmt parseStatement() {
    if (reader.advanceIfMatch(TokenType.PRINT)) {
      return parsePrintStatement();
    }

    if (reader.advanceIfMatch(TokenType.ASSERT)) {
      return parseAssertStatement();
    }

    if (reader.advanceIfMatch(TokenType.VAR)) {
      return parseVarStatement();
    }

    return parseExpressionStatement();
  }

  /** Parses a `print` statement. */
  private Stmt parsePrintStatement() {
    return new PrintStmt(parseExpression());
  }

  /** Parses a `assert` statement. */
  private Stmt parseAssertStatement() {
    return new AssertStmt(parseExpression());
  }

  /** Parses a `var` statement. */
  private Stmt parseVarStatement() {
    final var name = reader.consumeExpected(TokenType.IDENTIFIER, "Expected variable name.");

    if (reader.advanceIfMatch(TokenType.EQUAL)) {
      return new VarStmt(name, parseExpression());
    } else {
      return new VarStmt(name, null);
    }
  }

  /** Parses an expression statement. */
  private Stmt parseExpressionStatement() {
    return new ExpressionStmt(parseExpression());
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
      expr = new BinaryExpr(expr, operator, right);
    }

    return expr;
  }

  private Expr parseComparison() {
    var expr = parseTerm();

    while (reader.advanceIfMatch(
        TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      final var operator = reader.previous();
      final var right = parseTerm();
      expr = new BinaryExpr(expr, operator, right);
    }

    return expr;
  }

  private Expr parseTerm() {
    var expr = parseFactor();

    while (reader.advanceIfMatch(TokenType.MINUS, TokenType.PLUS)) {
      final var operator = reader.previous();
      final var right = parseFactor();
      expr = new BinaryExpr(expr, operator, right);
    }

    return expr;
  }

  private Expr parseFactor() {
    Expr expr = parseUnary();

    while (reader.advanceIfMatch(TokenType.SLASH, TokenType.STAR)) {
      final var operator = reader.previous();
      final var right = parseUnary();
      expr = new BinaryExpr(expr, operator, right);
    }

    return expr;
  }

  private Expr parseUnary() {
    if (reader.advanceIfMatch(TokenType.BANG, TokenType.MINUS)) {
      final var operator = reader.previous();
      final var right = parseUnary();
      return new UnaryExpr(operator, right);
    }

    return parsePrimary();
  }

  /** Parses a single primary-expression. */
  private Expr parsePrimary() {
    if (reader.advanceIfMatch(TokenType.FALSE)) {
      return new LiteralExpr(reader.previous(), false);
    }

    if (reader.advanceIfMatch(TokenType.TRUE)) {
      return new LiteralExpr(reader.previous(), true);
    }

    if (reader.advanceIfMatch(TokenType.NULL)) {
      return new LiteralExpr(reader.previous(), null);
    }

    if (reader.advanceIfMatch(TokenType.NUMBER, TokenType.STRING)) {
      return new LiteralExpr(reader.previous(), reader.previous().literal());
    }

    if (reader.advanceIfMatch(TokenType.LEFT_PAREN)) {
      final var expr = parseExpression();
      reader.consumeExpected(TokenType.RIGHT_PAREN, "Expected ')' after expression.");
      return new GroupingExpr(expr);
    }

    if (reader.advanceIfMatch(TokenType.IDENTIFIER)) {
      return new VariableExpr(reader.previous());
    }

    throw new ParserException(reader.peek(), "Expected expression.");
  }
}
