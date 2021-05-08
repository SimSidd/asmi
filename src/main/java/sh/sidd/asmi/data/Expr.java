package sh.sidd.asmi.data;

/**
 * Base interface for all expressions.
 */
public interface Expr {

  /**
   * Visitor pattern to visit each expression.
   *
   * @param <R> The type of the visitor return value.
   */
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
   * @param <R>     The type of the visitor return value.
   * @return The return value of the visitor.
   */
  <R> R accept(Visitor<R> visitor);

  record Binary(Expr left, Token operator, Expr right) implements Expr {

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }
  }

  record Grouping(Expr expression) implements Expr {

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
  }

  record Literal(Object value) implements Expr {

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
  }

  record Unary(Token operator, Expr right) implements Expr {

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
  }
}
