package sh.sidd.asmi.parser;

import sh.sidd.asmi.ErrorHandler;
import sh.sidd.asmi.data.Expr;
import sh.sidd.asmi.data.Expr.*;
import sh.sidd.asmi.data.Stmt;
import sh.sidd.asmi.data.Stmt.*;
import sh.sidd.asmi.data.Token;
import sh.sidd.asmi.data.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses a list of {@link Token} into an abstract syntax tree, i.e a list of {@link Stmt}.
 *
 * See {@link sh.sidd.asmi.scanner.Scanner} on how {@link Token} are generated.
 * See {@link sh.sidd.asmi.compiler.Compiler} on how {@link Stmt} are compiled.
 */
public class Parser {

  private final ErrorHandler errorHandler;
  private final TokenReader reader;

  public Parser(ErrorHandler errorHandler, List<Token> tokens) {
    this.errorHandler = errorHandler;
    this.reader = new TokenReader(tokens);
  }

  /**
   * Parses the given list of tokens into statements.
   *
   * @return The parsed statements.
   */
  public List<Stmt> parse() {
    final var statements = new ArrayList<Stmt>();

    while (!reader.isAtEnd()) {
      try {
        statements.add(parseStatement());
      } catch (ParserException ex) {
        errorHandler.report(ex.getToken(), ex.getMessage());
      }
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

    if (reader.advanceIfMatch(TokenType.DEF)) {
      return parseDefStatement();
    }

    if (reader.advanceIfMatch(TokenType.IF)) {
      return parseIfStatement();
    }

    if (reader.advanceIfMatch(TokenType.WHILE)) {
      return parseWhileStatement();
    }

    return parseAssignmentOrExpressionStatement();
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

  /**
   * Parses statements until a given end token appears.
   *
   * The end-token should still be verified and consumed.
   */
  private BlockStmt parseBlock(TokenType ...endTokens) {
    final var statements = new ArrayList<Stmt>();

    while(!reader.isAtEnd() && !reader.check(endTokens)) {
      statements.add(parseStatement());
    }

    return new BlockStmt(statements);
  }

  /** Parses a `def` statement. */
  private DefStmt parseDefStatement() {
    final var name = reader.consumeExpected(TokenType.IDENTIFIER, "Expected method name.");
    final var block = parseBlock(TokenType.END);

    reader.consumeExpected(TokenType.END, "Expected 'end' after method block.");

    return new DefStmt(name, block);
  }

  /** Parses a `if` statement. */
  private IfStmt parseIfStatement() {
    final var condition = parseExpression();
    final var thenBlock = parseBlock(TokenType.ELSE, TokenType.END);
    Stmt elseBlock = null;

    if(reader.advanceIfMatch(TokenType.ELSE)) {
      elseBlock = parseBlock(TokenType.END);
      reader.consumeExpected(TokenType.END, "Expected 'end' after 'else' block.");
    } else {
      reader.consumeExpected(TokenType.END, "Expected 'end' after 'if' block.");
    }

    return new IfStmt(condition, thenBlock, elseBlock);
  }

  /** Parses a `while` statement. */
  private WhileStmt parseWhileStatement() {
    final var condition = parseExpression();
    final var block = parseBlock(TokenType.END);

    reader.consumeExpected(TokenType.END, "Expected 'end' after 'while' block.");

    return new WhileStmt(condition, block);
  }

  /** Parses a statement which is either an assignment or an expression. */
  private Stmt parseAssignmentOrExpressionStatement() {
    final var expr = parseExpression();

    if(reader.advanceIfMatch(TokenType.EQUAL)) {
      final var equalsToken = reader.previous();
      final var value = parseExpression();

      if(expr instanceof Expr.VariableExpr varExrp) {
        return new AssignStmt(varExrp.getName(), value);
      }

      errorHandler.report(equalsToken, "Invalid assignment target.");
    }

    return new ExpressionStmt(expr);
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
