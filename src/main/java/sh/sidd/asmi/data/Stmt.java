package sh.sidd.asmi.data;

import java.util.List;
import lombok.Getter;

/** Base interface for all statements. */
public abstract class Stmt {

  /** Visitor pattern to visit each statement. */
  public interface Visitor<R> {
    R visitExpressionStmt(ExpressionStmt stmt);

    R visitPrintStmt(PrintStmt stmt);

    R visitAssertStmt(AssertStmt stmt);

    R visitVarStmt(VarStmt stmt);

    R visitAssignStmt(AssignStmt stmt);

    R visitBlockStmt(BlockStmt stmt);

    R visitDefStmt(DefStmt stmt);

    R visitIfStmt(IfStmt stmt);
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

  public static class AssignStmt extends Stmt {
    @Getter private final Token name;
    @Getter private final Expr value;

    public AssignStmt(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignStmt(this);
    }
  }

  public static class BlockStmt extends Stmt {
    @Getter private final List<Stmt> statements;

    public BlockStmt(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }
  }

  public static class DefStmt extends Stmt {
    @Getter private final Token name;
    @Getter private final Stmt block;

    public DefStmt(Token name, Stmt block) {
      this.name = name;
      this.block = block;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitDefStmt(this);
    }
  }

  public static class IfStmt extends Stmt {
    @Getter private final Expr condition;
    @Getter private final Stmt thenBlock;
    @Getter private final Stmt elseBlock;

    public IfStmt(Expr condition, Stmt thenBlock, Stmt elseBlock) {
      this.condition = condition;
      this.thenBlock = thenBlock;
      this.elseBlock = elseBlock;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }
  }
}
