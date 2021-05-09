package sh.sidd.asmi.e2e;

import org.junit.jupiter.api.Test;

public class IfElseTests {

  @Test
  public void shouldExecuteIfWithoutElse() {
    final var source = """
        if true
          print "test one"
        end
        
        if false
          print "test two"
        end
        
        if true
          print "test three"
        end
        """;

    AsmiTestUtil.assertOutput(source, "test one\ntest three");
  }

  @Test
  public void shouldHandleElseBlocks() {
    final var source = """
        if true
          print "test one"
        else
          print "test two"
        end
        
        if false
          print "test three"
        else
          print "test four"
        end
        """;

    AsmiTestUtil.assertOutput(source, "test one\ntest four");
  }

  @Test
  public void shouldHandleExpressionsInConditions() {
    final var source = """
        var v1 = 10
        var v2 = 20
        
        if v1 * 2 == v2
          print "test one"
        else
          print "test two"
        end
        
        if v1 == v2
          print "test three"
        else
          print "test four"
        end
        """;

    AsmiTestUtil.assertOutput(source, "test one\ntest four");
  }
}
