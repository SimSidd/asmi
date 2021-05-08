package sh.sidd.asmi.compiler;

import sh.sidd.asmi.data.Expr;
import sh.sidd.asmi.data.Expr.Binary;
import sh.sidd.asmi.data.Expr.Grouping;
import sh.sidd.asmi.data.Expr.Literal;
import sh.sidd.asmi.data.Expr.Unary;
import sh.sidd.asmi.data.Stmt;
import sh.sidd.asmi.data.Stmt.Expression;
import sh.sidd.asmi.data.Stmt.Print;
import sh.sidd.asmi.data.ValueType;

/** Visitor which determines the {@link ValueType} for a given expression. */
public class ValueTypeVisitor implements Expr.Visitor<ValueType>, Stmt.Visitor<ValueType> {

  @Override
  public ValueType visitBinaryExpr(Binary expr) {
    final var leftType = expr.getLeft().accept(this);
    final var rightType = expr.getRight().accept(this);
    final var valueType = ValueType.findImplicitCastType(leftType, rightType);

    expr.setValueType(valueType);

    return valueType;
  }

  @Override
  public ValueType visitGroupingExpr(Grouping expr) {
    final var valueType = expr.getExpr().accept(this);
    expr.setValueType(valueType);
    return valueType;
  }

  @Override
  public ValueType visitLiteralExpr(Literal expr) {
    final var valueType = ValueType.fromLiteral(expr.getValue());
    expr.setValueType(valueType);
    return valueType;
  }

  @Override
  public ValueType visitUnaryExpr(Unary expr) {
    final var valueType = expr.getRight().accept(this);
    expr.setValueType(valueType);
    return valueType;
  }

  @Override
  public ValueType visitExpression(Expression stmt) {
    final var valueType = stmt.expression().accept(this);
    stmt.expression().setValueType(valueType);
    return valueType;
  }

  @Override
  public ValueType visitPrint(Print stmt) {
    stmt.expression().accept(this);
    return null;
  }
}
