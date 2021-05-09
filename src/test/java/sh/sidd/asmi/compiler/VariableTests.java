package sh.sidd.asmi.compiler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import sh.sidd.asmi.TestUtil;

public class VariableTests {

  @Test
  void shouldNotAllowVariableRedefinition() {
    final var errorHandler = TestUtil.compileSource("var v1=0\nvar v1=0");
    TestUtil.assertError("Error at 'v1': Identifier already exists.", errorHandler);
  }
}
