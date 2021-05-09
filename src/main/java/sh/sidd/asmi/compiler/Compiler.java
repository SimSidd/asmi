package sh.sidd.asmi.compiler;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import sh.sidd.asmi.ErrorHandler;
import sh.sidd.asmi.data.Expr;
import sh.sidd.asmi.data.Expr.BinaryExpr;
import sh.sidd.asmi.data.Expr.GroupingExpr;
import sh.sidd.asmi.data.Expr.LiteralExpr;
import sh.sidd.asmi.data.Expr.UnaryExpr;
import sh.sidd.asmi.data.Expr.VariableExpr;
import sh.sidd.asmi.data.Stmt;
import sh.sidd.asmi.data.Stmt.AssertStmt;
import sh.sidd.asmi.data.Stmt.AssignStmt;
import sh.sidd.asmi.data.Stmt.ExpressionStmt;
import sh.sidd.asmi.data.Stmt.PrintStmt;
import sh.sidd.asmi.data.Stmt.VarStmt;
import sh.sidd.asmi.data.ValueType;
import sh.sidd.asmi.scanner.SourceRetriever;

@Slf4j
public class Compiler implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

  private final ErrorHandler errorHandler;
  private final ByteCodeWriter writer;
  private final List<Stmt> ast;
  private final SourceRetriever sourceRetriever;
  private final VariableEnv variableEnv = new VariableEnv();

  public Compiler(ErrorHandler errorHandler, List<Stmt> ast,
      SourceRetriever sourceRetriever) {
    this.errorHandler = errorHandler;
    this.sourceRetriever = sourceRetriever;
    writer = new ByteCodeWriter();
    this.ast = ast;
  }

  /** Compiles the AST into a .class file. */
  public void compile() {
    final var valueTypeVisitor = new ValueTypeVisitor(variableEnv, errorHandler);
    final var sourceLineVisitor = new SourceLineVisitor();

    if(ast == null) {
      return;
    }

    for(final var stmt : ast) {
      stmt.accept(valueTypeVisitor);
      stmt.accept(sourceLineVisitor);
    }

    writer.startClass();
    writer.startMethod();

    for(final var stmt : ast) {
      stmt.accept(this);
    }

    if(!errorHandler.hasErrors()) {
      writer.endMethod();
      writer.finishClass();
    }
  }

  public void run() throws Throwable {
    if(!errorHandler.hasErrors()) {
      writer.run();
    }
  }

  @Override
  public Void visitBinaryExpr(BinaryExpr expr) {
    final var leftType = expr.getLeft().getValueType();
    final var rightType = expr.getRight().getValueType();
    final var resultType = ValueType.findImplicitCastType(leftType, rightType);

    if(!resultType.isNumeric()) {
      errorHandler.report(expr.getOperator(), "Operands must be numeric.");
      return null;
    }

    try {
      expr.getLeft().accept(this);
      if(leftType != resultType) {
        writer.writeCast(leftType, resultType);
      }

      expr.getRight().accept(this);
      if(rightType != resultType) {
        writer.writeCast(rightType, resultType);
      }

      switch(expr.getOperator().tokenType()) {
        case PLUS -> writer.writeAdd(resultType);
        case MINUS -> writer.writeSub(resultType);
        case STAR -> writer.writeMul(resultType);
        case SLASH -> writer.writeDiv(resultType);
        case EQUAL_EQUAL -> writer.writeCmp(resultType);
        case BANG_EQUAL -> {
          writer.writeCmp(resultType);
          writer.writeNeg(resultType);
        }
        default -> errorHandler.report(expr.getOperator(), "Expected binary operator.");
      }
    } catch (ByteCodeException ex) {
      errorHandler.report(expr.getOperator(), ex.getMessage());
    }

    return null;
  }

  @Override
  public Void visitGroupingExpr(GroupingExpr expr) {
    return expr.getExpr().accept(this);
  }

  @Override
  public Void visitLiteralExpr(LiteralExpr expr) {
    writer.writeConstant(expr.getValue());
    return null;
  }

  @Override
  public Void visitUnaryExpr(UnaryExpr expr) {
    final var rightType = expr.getRight().getValueType();

    try {
      switch(expr.getOperator().tokenType()) {
        case MINUS -> {
          if(!rightType.isNumeric()) {
            errorHandler.report(expr.getOperator(), "Can only negate numeric values.");
            return null;
          }

          expr.getRight().accept(this);
          writer.writeNeg(rightType);
        }
        default -> errorHandler.report(expr.getOperator(), "Expected unary operator.");
      }
    } catch (ByteCodeException ex) {
      errorHandler.report(expr.getOperator(), ex.getMessage());
    }

    return null;
  }

  @Override
  public Void visitVariableExpr(VariableExpr expr) {
    try {
      writer.loadVariable(expr.getValueType(),
          variableEnv.getVariableIndex(expr.getName().lexeme()));
    } catch (VariableEnvException e) {
      errorHandler.report(expr.getName(), e.getMessage());
    }

    return null;
  }

  @Override
  public Void visitExpressionStmt(ExpressionStmt stmt) {
    stmt.getExpression().accept(this);

    return null;
  }

  @Override
  public Void visitPrintStmt(PrintStmt stmt) {
    final var valueType = stmt.getExpression().getValueType();

    writer.writePrint(valueType, () -> stmt.getExpression().accept(this));

    return null;
  }

  @Override
  public Void visitAssertStmt(AssertStmt stmt) {
    stmt.getExpression().accept(this);

    writer.writeAssert(sourceRetriever.getLines(
        stmt.getExpression().getLineStart(),
        stmt.getExpression().getLineEnd()));

    return null;
  }

  @Override
  public Void visitVarStmt(VarStmt stmt) {
    if(stmt.getInitializer() != null) {
      stmt.getInitializer().accept(this);
    }

    try {
      writer.storeVariable(stmt.getInitializer().getValueType(),
          variableEnv.getVariableIndex(stmt.getName().lexeme()));
    } catch (VariableEnvException e) {
      errorHandler.report(stmt.getName(), e.getMessage());
    }

    return null;
  }

  @Override
  public Void visitAssignStmt(AssignStmt stmt) {
    stmt.getValue().accept(this);

    try {
      writer.storeVariable(stmt.getValue().getValueType(),
          variableEnv.getVariableIndex(stmt.getName().lexeme()));
    } catch (VariableEnvException e) {
      errorHandler.report(stmt.getName(), e.getMessage());
    }

    return null;
  }
}
