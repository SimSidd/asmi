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
import sh.sidd.asmi.data.Stmt.BlockStmt;
import sh.sidd.asmi.data.Stmt.DefStmt;
import sh.sidd.asmi.data.Stmt.ExpressionStmt;
import sh.sidd.asmi.data.Stmt.IfStmt;
import sh.sidd.asmi.data.Stmt.PrintStmt;
import sh.sidd.asmi.data.Stmt.VarStmt;

/** Visitor which determines the source lines for expressions. */
public class SourceLineVisitor implements Expr.Visitor<Pair<Integer, Integer>>, Stmt.Visitor<Void> {

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
  public Void visitExpressionStmt(ExpressionStmt stmt) {
    stmt.getExpression().accept(this);
    return null;
  }

  @Override
  public Void visitPrintStmt(PrintStmt stmt) {
    stmt.getExpression().accept(this);
    return null;
  }

  @Override
  public Void visitAssertStmt(AssertStmt stmt) {
    stmt.getExpression().accept(this);
    return null;
  }

  @Override
  public Void visitVarStmt(VarStmt stmt) {
    stmt.getInitializer().accept(this);
    return null;
  }

  @Override
  public Void visitAssignStmt(AssignStmt stmt) {
    stmt.getValue().accept(this);
    return null;
  }

  @Override
  public Void visitBlockStmt(BlockStmt stmt) {
    for (final var s : stmt.getStatements()) {
      s.accept(this);
    }

    return null;
  }

  @Override
  public Void visitDefStmt(DefStmt stmt) {
    stmt.getBlock().accept(this);
    return null;
  }

  @Override
  public Void visitIfStmt(IfStmt stmt) {
    stmt.getCondition().accept(this);
    stmt.getThenBlock().accept(this);

    if (stmt.getElseBlock() != null) {
      stmt.getElseBlock().accept(this);
    }

    return null;
  }
}
