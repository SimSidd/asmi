package sh.sidd.asmi.data;

import lombok.Getter;

/** Base interface for all statements. */
public interface Stmt {

  /** Visitor pattern to visit each statement. */
  interface Visitor<R> {
    R visitExpression(Expression stmt);

    R visitPrint(Print stmt);

    R visitAssert(Assert stmt);
  }

  /**
   * Accepts a visitor and visits this statement.
   *
   * @param visitor The visitor to use.
   */
  <R> R accept(Visitor<R> visitor);

  class Expression implements Stmt {
    @Getter private final Expr expression;

    public Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpression(this);
    }
  }

  class Print implements Stmt {
    @Getter private final Expr expression;

    public Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrint(this);
    }
  }

  class Assert implements Stmt {
    @Getter private final Expr expression;

    public Assert(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssert(this);
    }
  }
}
