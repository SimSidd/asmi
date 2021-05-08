package sh.sidd.asmi.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Base interface for all expressions. */
public interface Expr {

  /** Visitor pattern to visit each expression. */
  interface Visitor<R> {
    R visitBinaryExpr(Binary expr);

    R visitGroupingExpr(Grouping expr);

    R visitLiteralExpr(Literal expr);

    R visitUnaryExpr(Unary expr);
  }

  /**
   * Accepts a visitor and visits this expression.
   *
   * @param visitor The visitor to use.
   */
  <R> R accept(Visitor<R> visitor);

  /** Returns the type of this expression. */
  ValueType getValueType();

  /**
   * Sets the type of this expression.
   *
   * @param valueType The type of this expression.
   */
  void setValueType(ValueType valueType);

  @ToString
  class Binary implements Expr {
    @Getter private final Expr left;
    @Getter private final Token operator;
    @Getter private final Expr right;
    @Getter @Setter private ValueType valueType;

    public Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }
  }

  @ToString
  class Grouping implements Expr {
    @Getter private final Expr expr;
    @Getter @Setter private ValueType valueType;

    public Grouping(Expr expr) {
      this.expr = expr;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
  }

  @ToString
  class Literal implements Expr {
    @Getter private final Object value;
    @Getter @Setter private ValueType valueType;

    public Literal(Object value) {
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
  }

  @ToString
  class Unary implements Expr {
    @Getter private final Token operator;
    @Getter private final Expr right;
    @Getter @Setter private ValueType valueType;

    public Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
  }
}
