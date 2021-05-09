package sh.sidd.asmi.e2e;

import org.junit.jupiter.api.Test;

public class DefTests {

  @Test
  public void shouldAllowDefStatements() {
    final var source = """
        def method1
        end
        
        def method2
        end
        """;

    AsmiTestUtil.assertNoErrors(source);
  }
}
