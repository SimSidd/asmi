package sh.sidd.asmi.compiler;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import sh.sidd.asmi.ErrorHandler;
import sh.sidd.asmi.data.Expr;
import sh.sidd.asmi.data.Expr.Binary;
import sh.sidd.asmi.data.Expr.Grouping;
import sh.sidd.asmi.data.Expr.Literal;
import sh.sidd.asmi.data.Expr.Unary;
import sh.sidd.asmi.data.Stmt;
import sh.sidd.asmi.data.Stmt.Expression;
import sh.sidd.asmi.data.Stmt.Print;
import sh.sidd.asmi.data.ValueType;

@Slf4j
public class Compiler implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

  private final ErrorHandler errorHandler;
  private final ByteCodeWriter writer;
  private final List<Stmt> ast;
  private final ValueTypeVisitor valueTypeVisitor;

  public Compiler(ErrorHandler errorHandler, List<Stmt> ast) {
    this.errorHandler = errorHandler;
    writer = new ByteCodeWriter();
    this.ast = ast;
    this.valueTypeVisitor = new ValueTypeVisitor();
  }

  /** Compiles the AST into a .class file. */
  public void compile() {
    writer.startClass();

    writer.startMethod();

    for(final var stmt : ast) {
      stmt.accept(this);
    }

    writer.endMethod();

    writer.finishClass();

    if(!errorHandler.isHasError()) {
      writer.run();
    }
  }

  @Override
  public Void visitBinaryExpr(Binary expr) {
    final var leftType = expr.left().accept(valueTypeVisitor);
    final var rightType = expr.right().accept(valueTypeVisitor);
    final var resultType = ValueType.findImplicitCastType(leftType, rightType);

    if(!resultType.isNumeric()) {
      errorHandler.report(expr.operator(), "Operands must be numeric.");
      return null;
    }

    try {
      expr.left().accept(this);
      if(leftType != resultType) {
        writer.writeCast(leftType, resultType);
      }

      expr.right().accept(this);
      if(rightType != resultType) {
        writer.writeCast(rightType, resultType);
      }

      switch(expr.operator().tokenType()) {
        case PLUS -> writer.writeAdd(resultType);
        case MINUS -> writer.writeSub(resultType);
        case STAR -> writer.writeMul(resultType);
        case SLASH -> writer.writeDiv(resultType);
        default -> errorHandler.report(expr.operator(), "Expected binary operator.");
      }
    } catch (ByteCodeException ex) {
      errorHandler.report(expr.operator(), ex.getMessage());
    }

    return null;
  }

  @Override
  public Void visitGroupingExpr(Grouping expr) {
    return expr.accept(this);
  }

  @Override
  public Void visitLiteralExpr(Literal expr) {
    writer.pushConstant(expr.value());
    return null;
  }

  @Override
  public Void visitUnaryExpr(Unary expr) {
    final var rightType = expr.right().accept(valueTypeVisitor);

    try {
      switch(expr.operator().tokenType()) {
        case MINUS -> {
          if(!rightType.isNumeric()) {
            errorHandler.report(expr.operator(), "Can only negate numeric values.");
            return null;
          }

          expr.right().accept(this);

          writer.pushConstant(-1);
          writer.writeMul(rightType);
        }
        default -> errorHandler.report(expr.operator(), "Expected unary operator.");
      }
    } catch (ByteCodeException ex) {
      errorHandler.report(expr.operator(), ex.getMessage());
    }

    return null;
  }

  @Override
  public Void visitExpression(Expression stmt) {
    stmt.expression().accept(this);

    return null;
  }

  @Override
  public Void visitPrint(Print stmt) {
    final var valueType = stmt.expression().accept(valueTypeVisitor);

    writer.writePrint(valueType, () -> stmt.expression().accept(this));

    return null;
  }
}
