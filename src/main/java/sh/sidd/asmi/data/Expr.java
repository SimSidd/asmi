package sh.sidd.asmi.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Base interface for all expressions. */
public abstract class Expr {
  @Getter @Setter private ValueType valueType;
  @Getter @Setter private int lineStart;
  @Getter @Setter private int lineEnd;

  /** Visitor pattern to visit each expression. */
  public interface Visitor<R> {
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
  public abstract <R> R accept(Visitor<R> visitor);

  @ToString
  public static class Binary extends Expr {
    @Getter private final Expr left;
    @Getter private final Token operator;
    @Getter private final Expr right;

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
  public static class Grouping extends Expr {
    @Getter private final Expr expr;

    public Grouping(Expr expr) {
      this.expr = expr;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
  }

  @ToString
  public static class Literal extends Expr {
    @Getter private final Token token;
    @Getter private final Object value;

    public Literal(Token token, Object value) {
      this.token = token;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
  }

  @ToString
  public static class Unary extends Expr {
    @Getter private final Token operator;
    @Getter private final Expr right;

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
