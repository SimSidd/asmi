package sh.sidd.asmi.compiler;

import sh.sidd.asmi.data.Expr.Binary;
import sh.sidd.asmi.data.Expr.Grouping;
import sh.sidd.asmi.data.Expr.Literal;
import sh.sidd.asmi.data.Expr.Unary;
import sh.sidd.asmi.data.Expr.Visitor;
import sh.sidd.asmi.data.ValueType;

/** Visitor which determines the {@link ValueType} for a given expression. */
public class ValueTypeVisitor implements Visitor<ValueType> {

  @Override
  public ValueType visitBinaryExpr(Binary expr) {
    final var leftType = expr.left().accept(this);
    final var rightType = expr.right().accept(this);

    return ValueType.findImplicitCastType(leftType, rightType);
  }

  @Override
  public ValueType visitGroupingExpr(Grouping expr) {
    return expr.accept(this);
  }

  @Override
  public ValueType visitLiteralExpr(Literal expr) {
    return ValueType.fromLiteral(expr.value());
  }

  @Override
  public ValueType visitUnaryExpr(Unary expr) {
    return expr.right().accept(this);
  }
}
