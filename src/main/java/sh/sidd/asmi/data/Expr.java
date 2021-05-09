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
    R visitBinaryExpr(BinaryExpr expr);

    R visitGroupingExpr(GroupingExpr expr);

    R visitLiteralExpr(LiteralExpr expr);

    R visitUnaryExpr(UnaryExpr expr);

    R visitVariableExpr(VariableExpr expr);
  }

  /**
   * Accepts a visitor and visits this expression.
   *
   * @param visitor The visitor to use.
   */
  public abstract <R> R accept(Visitor<R> visitor);

  @ToString
  public static class BinaryExpr extends Expr {
    @Getter private final Expr left;
    @Getter private final Token operator;
    @Getter private final Expr right;

    public BinaryExpr(Expr left, Token operator, Expr right) {
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
  public static class GroupingExpr extends Expr {
    @Getter private final Expr expr;

    public GroupingExpr(Expr expr) {
      this.expr = expr;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
  }

  @ToString
  public static class LiteralExpr extends Expr {
    @Getter private final Token token;
    @Getter private final Object value;

    public LiteralExpr(Token token, Object value) {
      this.token = token;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
  }

  @ToString
  public static class UnaryExpr extends Expr {
    @Getter private final Token operator;
    @Getter private final Expr right;

    public UnaryExpr(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
  }

  @ToString
  public static class VariableExpr extends Expr {
    @Getter private final Token name;

    public VariableExpr(Token name) {
      this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }
  }
}
