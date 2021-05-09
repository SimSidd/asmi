package sh.sidd.asmi.data;

import lombok.Getter;

/** Base interface for all statements. */
public abstract class Stmt {

  /** Visitor pattern to visit each statement. */
  public interface Visitor<R> {
    R visitExpression(ExpressionStatement stmt);

    R visitPrint(Print stmt);

    R visitAssert(Assert stmt);
  }

  /**
   * Accepts a visitor and visits this statement.
   *
   * @param visitor The visitor to use.
   */
  public abstract <R> R accept(Visitor<R> visitor);

  public static class ExpressionStatement extends Stmt {
    @Getter private final Expr expression;

    public ExpressionStatement(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpression(this);
    }
  }

  public static class Print extends Stmt {
    @Getter private final Expr expression;

    public Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrint(this);
    }
  }

  public static class Assert extends Stmt {
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
