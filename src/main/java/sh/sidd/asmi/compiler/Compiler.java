package sh.sidd.asmi.compiler;

import lombok.extern.slf4j.Slf4j;
import sh.sidd.asmi.ErrorHandler;
import sh.sidd.asmi.data.Expr;
import sh.sidd.asmi.data.Expr.*;
import sh.sidd.asmi.data.Stmt;
import sh.sidd.asmi.data.Stmt.*;
import sh.sidd.asmi.data.ValueType;
import sh.sidd.asmi.scanner.SourceRetriever;

import java.util.List;

/**
 * Compiles {@link Stmt} into java bytecode.
 *
 * See {@link sh.sidd.asmi.parser.Parser} on how {@link Stmt} are generated.
 */
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

  /**
   * Compiles the AST into a .class file.
   *
   * Currently, all code is written into a single method.
   */
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

    writer.startClass("sh/sidd/asmi/Compiled");
    writer.startMethod("main");

    for(final var stmt : ast) {
      stmt.accept(this);
    }

    if(!errorHandler.hasErrors()) {
      writer.endMethod();
      writer.finishClass();
    }
  }

  /**
   * Returns the String representation of the compiled bytecode.
   */
  public String getByteCode() {
    return writer.getWrittenByteCode();
  }

  /**
   * Runs the written source.
   *
   * Currently, all code is compiled to a single method.
   *
   * @throws Throwable For exceptions during execution.
   */
  public void run() throws Throwable {
    if(!errorHandler.hasErrors()) {
      writer.invokeMethod("sh.sidd.asmi.Compiled", "main");
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
        case LESS -> writer.writeLt(resultType);
        case LESS_EQUAL -> writer.writeLe(resultType);
        case GREATER -> writer.writeGt(resultType);
        case GREATER_EQUAL -> writer.writeGe(resultType);
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

  @Override
  public Void visitBlockStmt(BlockStmt stmt) {
    for(final var s : stmt.getStatements()) {
      s.accept(this);
    }

    return null;
  }

  @Override
  public Void visitDefStmt(DefStmt stmt) {
    stmt.getBlock().accept(this);
    return null;
  }

  @Override
  public Void visitIfStmt(IfStmt stmt) {
    Runnable elseRunnable = null;

    if(stmt.getElseBlock() != null) {
      elseRunnable = () -> stmt.getElseBlock().accept(this);
    }

    writer.writeIfThenElse(
        () -> stmt.getCondition().accept(this),
        () -> stmt.getThenBlock().accept(this),
        elseRunnable);

    return null;
  }

  @Override
  public Void visitWhileStmt(WhileStmt stmt) {
    writer.writeWhile(
        () -> stmt.getCondition().accept(this),
        () -> stmt.getBlock().accept(this));

    return null;
  }
}
