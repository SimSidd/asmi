package sh.sidd.asmi.data;

/**
 * Base interface for all statements.
 */
public interface Stmt {

  /**
   * Visitor pattern to visit each statement.
   */
  interface Visitor<R> {
    R visitExpression(Expression stmt);
    R visitPrint(Print stmt);
  }

  /**
   * Accepts a visitor and visits this statement.
   *
   * @param visitor The visitor to use.
   */
  <R> R accept(Visitor<R> visitor);

  record Expression(Expr expression) implements Stmt {

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpression(this);
    }
  }

  record Print(Expr expression) implements Stmt {
    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrint(this);
    }
  }
}
