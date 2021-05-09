package sh.sidd.asmi.data;

import lombok.Getter;

/** Base interface for all statements. */
public abstract class Stmt {

  /** Visitor pattern to visit each statement. */
  public interface Visitor<R> {
    R visitExpressionStmt(ExpressionStmt stmt);

    R visitPrintStmt(PrintStmt stmt);

    R visitAssertStmt(AssertStmt stmt);

    R visitVarStmt(VarStmt stmt);
  }

  /**
   * Accepts a visitor and visits this statement.
   *
   * @param visitor The visitor to use.
   */
  public abstract <R> R accept(Visitor<R> visitor);

  public static class ExpressionStmt extends Stmt {
    @Getter private final Expr expression;

    public ExpressionStmt(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }
  }

  public static class PrintStmt extends Stmt {
    @Getter private final Expr expression;

    public PrintStmt(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }
  }

  public static class AssertStmt extends Stmt {
    @Getter private final Expr expression;

    public AssertStmt(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssertStmt(this);
    }
  }

  public static class VarStmt extends Stmt {
    @Getter private final Token name;
    @Getter private final Expr initializer;

    public VarStmt(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }
  }
}
