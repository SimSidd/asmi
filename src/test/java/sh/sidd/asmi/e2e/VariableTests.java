package sh.sidd.asmi.e2e;

import org.junit.jupiter.api.Test;

public class VariableTests {

  @Test
  void shouldSupportVariableDeclaration() {
    final var source = """
        var v1 = 100
        var v2 = 200
        var v3 = 300
        
        assert v1 == 100
        assert v2 == 200
        assert v3 == 300
        
        assert v1 + v2 == v3
        
        assert v1 / v2 == 0
        assert (0.0 + v1) / v2 == 0.5
        """;

    AsmiTestUtil.assertNoErrors(source);
  }

  @Test
  void shouldSupportVariableAssignment() {
    final var source = """
        var v1 = 100
        var v2 = 200
        var v3 = 300
        
        assert v1 == 100
        assert v2 == 200
        assert v3 == 300
        
        v1 = 400
        v2 = 500
        v3 = 600
        
        assert v1 == 400
        assert v2 == 500
        assert v3 == 600
        
        v1 = v3
        v3 = v2
        v2 = 700
        
        assert v1 == 600
        assert v2 == 700
        assert v3 == 500
        """;

    AsmiTestUtil.assertNoErrors(source);
  }

  @Test
  void shouldNotAllowVariableRedefinition() {
    final var source = """
      var v1=0
      var v1=0
      """;

    AsmiTestUtil.assertCompileError(source, "Error at 'v1': Identifier already exists.");
  }

  @Test
  void shouldFailOnUndeclaredVariablesInAssignment() {
    final var source = """
      v = 0
      """;

    AsmiTestUtil.assertCompileError(source, "Error at 'v': Unknown variable: v");
  }

  @Test
  void shouldFailOnUndeclaredVariablesInExpression() {
    final var source = """
      assert v == 0
      """;

    // TODO Make this only one error. Currently it also reports "Operands must be numeric"
    AsmiTestUtil.assertCompileError(source, "Error at 'v': Unknown variable: v");
  }
}
