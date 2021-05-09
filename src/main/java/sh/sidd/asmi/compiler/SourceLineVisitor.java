package sh.sidd.asmi.compiler;

import org.apache.commons.lang3.tuple.Pair;
import sh.sidd.asmi.data.Expr;
import sh.sidd.asmi.data.Expr.BinaryExpr;
import sh.sidd.asmi.data.Expr.GroupingExpr;
import sh.sidd.asmi.data.Expr.LiteralExpr;
import sh.sidd.asmi.data.Expr.UnaryExpr;
import sh.sidd.asmi.data.Expr.VariableExpr;
import sh.sidd.asmi.data.Stmt;
import sh.sidd.asmi.data.Stmt.AssertStmt;
import sh.sidd.asmi.data.Stmt.AssignStmt;
import sh.sidd.asmi.data.Stmt.ExpressionStmt;
import sh.sidd.asmi.data.Stmt.PrintStmt;
import sh.sidd.asmi.data.Stmt.VarStmt;

/** Visitor which determines the source lines for expressions. */
public class SourceLineVisitor
    implements Expr.Visitor<Pair<Integer, Integer>>, Stmt.Visitor<Pair<Integer, Integer>> {

  @Override
  public Pair<Integer, Integer> visitBinaryExpr(BinaryExpr expr) {
    final var leftRange = expr.getLeft().accept(this);
    final var rightRange = expr.getRight().accept(this);
    final var resultRange = Pair.of(leftRange.getLeft(), rightRange.getRight());

    expr.setLineStart(resultRange.getLeft());
    expr.setLineEnd(resultRange.getRight());

    return resultRange;
  }

  @Override
  public Pair<Integer, Integer> visitGroupingExpr(GroupingExpr expr) {
    final var groupRange = expr.getExpr().accept(this);

    expr.setLineStart(groupRange.getLeft());
    expr.setLineEnd(groupRange.getRight());

    return groupRange;
  }

  @Override
  public Pair<Integer, Integer> visitLiteralExpr(LiteralExpr expr) {
    final var line = expr.getToken().line();
    final var range = Pair.of(line, line);

    expr.setLineStart(range.getLeft());
    expr.setLineEnd(range.getRight());

    return range;
  }

  @Override
  public Pair<Integer, Integer> visitUnaryExpr(UnaryExpr expr) {
    final var lineStart = expr.getOperator().line();
    final var rightRange = expr.getRight().accept(this);
    final var resultRange = Pair.of(lineStart, rightRange.getRight());

    expr.setLineStart(resultRange.getLeft());
    expr.setLineEnd(resultRange.getRight());

    return resultRange;
  }

  @Override
  public Pair<Integer, Integer> visitVariableExpr(VariableExpr expr) {
    final var line = expr.getName().line();

    expr.setLineStart(line);
    expr.setLineEnd(line);

    return Pair.of(line, line);
  }

  @Override
  public Pair<Integer, Integer> visitExpressionStmt(ExpressionStmt stmt) {
    final var range = stmt.getExpression().accept(this);

    stmt.getExpression().setLineStart(range.getLeft());
    stmt.getExpression().setLineEnd(range.getRight());

    return range;
  }

  @Override
  public Pair<Integer, Integer> visitPrintStmt(PrintStmt stmt) {
    return stmt.getExpression().accept(this);
  }

  @Override
  public Pair<Integer, Integer> visitAssertStmt(AssertStmt stmt) {
    return stmt.getExpression().accept(this);
  }

  @Override
  public Pair<Integer, Integer> visitVarStmt(VarStmt stmt) {
    final var initRange = stmt.getInitializer().accept(this);
    return Pair.of(stmt.getName().line(), initRange.getRight());
  }

  @Override
  public Pair<Integer, Integer> visitAssignStmt(AssignStmt stmt) {
    final var valueRange = stmt.getValue().accept(this);
    return Pair.of(stmt.getName().line(), valueRange.getRight());
  }
}
