package sh.sidd.asmi.e2e;

import org.junit.jupiter.api.Test;

public class WhileTests {
    @Test
    void shouldExecuteBasicWhileLoop() {
        // TODO implement LT for expressions
        final var source = """
            var i = 0
            
            while i != 3
              print i
              i = i + 1
            end
            """;

        AsmiTestUtil.assertOutput(source, "0\n1\n2\n");
    }
}
