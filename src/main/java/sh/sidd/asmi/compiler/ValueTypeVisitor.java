package sh.sidd.asmi.compiler;

import sh.sidd.asmi.ErrorHandler;
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
import sh.sidd.asmi.data.ValueType;

/** Visitor which determines the {@link ValueType} for expressions. */
public class ValueTypeVisitor implements Expr.Visitor<ValueType>, Stmt.Visitor<ValueType> {

  private final VariableEnv variableEnv;
  private final ErrorHandler errorHandler;

  public ValueTypeVisitor(VariableEnv variableEnv, ErrorHandler errorHandler) {
    this.variableEnv = variableEnv;
    this.errorHandler = errorHandler;
  }

  @Override
  public ValueType visitBinaryExpr(BinaryExpr expr) {
    final var leftType = expr.getLeft().accept(this);
    final var rightType = expr.getRight().accept(this);
    final var valueType = ValueType.findImplicitCastType(leftType, rightType);

    expr.setValueType(valueType);

    return valueType;
  }

  @Override
  public ValueType visitGroupingExpr(GroupingExpr expr) {
    final var valueType = expr.getExpr().accept(this);
    expr.setValueType(valueType);
    return valueType;
  }

  @Override
  public ValueType visitLiteralExpr(LiteralExpr expr) {
    final var valueType = ValueType.fromLiteral(expr.getValue());
    expr.setValueType(valueType);
    return valueType;
  }

  @Override
  public ValueType visitUnaryExpr(UnaryExpr expr) {
    final var valueType = expr.getRight().accept(this);
    expr.setValueType(valueType);
    return valueType;
  }

  @Override
  public ValueType visitVariableExpr(VariableExpr expr) {
    try {
      final var valueType = variableEnv.getVariableType(expr.getName().lexeme());
      expr.setValueType(valueType);
      return valueType;
    } catch (VariableEnvException e) {
      errorHandler.report(expr.getName(), e.getMessage());
    }

    return ValueType.UNKNOWN;
  }

  @Override
  public ValueType visitExpressionStmt(ExpressionStmt stmt) {
    final var valueType = stmt.getExpression().accept(this);
    stmt.getExpression().setValueType(valueType);
    return valueType;
  }

  @Override
  public ValueType visitPrintStmt(PrintStmt stmt) {
    stmt.getExpression().accept(this);
    return ValueType.UNKNOWN;
  }

  @Override
  public ValueType visitAssertStmt(AssertStmt stmt) {
    stmt.getExpression().accept(this);
    return ValueType.UNKNOWN;
  }

  @Override
  public ValueType visitVarStmt(VarStmt stmt) {
    try {
      variableEnv.defineVariable(stmt.getName().lexeme(), stmt.getInitializer().accept(this));
    } catch (VariableEnvException e) {
      errorHandler.report(stmt.getName(), e.getMessage());
    }

    return ValueType.UNKNOWN;
  }

  @Override
  public ValueType visitAssignStmt(AssignStmt stmt) {
    stmt.getValue().accept(this);
    return ValueType.UNKNOWN;
  }

  @Override
  public ValueType visitBlockStmt(BlockStmt stmt) {
    for(final var s : stmt.getStatements()) {
      s.accept(this);
    }

    return ValueType.UNKNOWN;
  }

  @Override
  public ValueType visitDefStmt(DefStmt stmt) {
    stmt.getBlock().accept(this);
    return ValueType.UNKNOWN;
  }

  @Override
  public ValueType visitIfStmt(IfStmt stmt) {
    stmt.getCondition().accept(this);
    stmt.getThenBlock().accept(this);

    if (stmt.getElseBlock() != null) {
      stmt.getElseBlock().accept(this);
    }

    return ValueType.UNKNOWN;
  }
}
