package sh.sidd.asmi.compiler;

import org.apache.commons.lang3.tuple.Pair;
import sh.sidd.asmi.data.Expr;
import sh.sidd.asmi.data.Expr.Binary;
import sh.sidd.asmi.data.Expr.Grouping;
import sh.sidd.asmi.data.Expr.Literal;
import sh.sidd.asmi.data.Expr.Unary;
import sh.sidd.asmi.data.Stmt;
import sh.sidd.asmi.data.Stmt.Assert;
import sh.sidd.asmi.data.Stmt.Expression;
import sh.sidd.asmi.data.Stmt.Print;
import sh.sidd.asmi.data.ValueType;

/** Visitor which determines the source lines for expressions. */
public class SourceLineVisitor
    implements Expr.Visitor<Pair<Integer, Integer>>, Stmt.Visitor<Pair<Integer, Integer>> {

  @Override
  public Pair<Integer, Integer> visitBinaryExpr(Binary expr) {
    final var leftRange = expr.getLeft().accept(this);
    final var rightRange = expr.getRight().accept(this);
    final var resultRange = Pair.of(leftRange.getLeft(), rightRange.getRight());

    expr.setLineStart(resultRange.getLeft());
    expr.setLineEnd(resultRange.getRight());

    return resultRange;
  }

  @Override
  public Pair<Integer, Integer> visitGroupingExpr(Grouping expr) {
    final var groupRange = expr.getExpr().accept(this);

    expr.setLineStart(groupRange.getLeft());
    expr.setLineEnd(groupRange.getRight());

    return groupRange;
  }

  @Override
  public Pair<Integer, Integer> visitLiteralExpr(Literal expr) {
    final var line = expr.getToken().line();
    final var range = Pair.of(line, line);

    expr.setLineStart(range.getLeft());
    expr.setLineEnd(range.getRight());

    return range;
  }

  @Override
  public Pair<Integer, Integer> visitUnaryExpr(Unary expr) {
    final var lineStart = expr.getOperator().line();
    final var rightRange = expr.getRight().accept(this);
    final var resultRange = Pair.of(lineStart, rightRange.getRight());

    expr.setLineStart(resultRange.getLeft());
    expr.setLineEnd(resultRange.getRight());

    return resultRange;
  }

  @Override
  public Pair<Integer, Integer> visitExpression(Expression stmt) {
    final var range = stmt.getExpression().accept(this);

    stmt.getExpression().setLineStart(range.getLeft());
    stmt.getExpression().setLineEnd(range.getRight());

    return range;
  }

  @Override
  public Pair<Integer, Integer> visitPrint(Print stmt) {
    final var range = stmt.getExpression().accept(this);

    stmt.getExpression().setLineStart(range.getLeft());
    stmt.getExpression().setLineEnd(range.getRight());

    return range;
  }

  @Override
  public Pair<Integer, Integer> visitAssert(Assert stmt) {
    final var range = stmt.getExpression().accept(this);

    stmt.getExpression().setLineStart(range.getLeft());
    stmt.getExpression().setLineEnd(range.getRight());

    return range;
  }
}
