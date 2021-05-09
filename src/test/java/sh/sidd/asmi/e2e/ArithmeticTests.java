package sh.sidd.asmi.e2e;

import org.junit.jupiter.api.Test;

public class ArithmeticTests {

  @Test
  public void shouldSupportBasicBinaryOperators() {
    final var source = """
        assert 1 + 1 == 2
        assert 3 - 1 == 2
        assert 3 * 2 == 6
        assert 6 / 2 == 3
        """;

    AsmiTestUtil.assertNoErrors(source);
  }

  @Test
  public void shouldDoBasicArithmetic() {
    final var source = """
        assert 1 - 2 * 3 == -5

        assert (1 - 2) * 3 == -3
        assert ((1 - 2) * 3) + 1 == -2
        
        assert 5 / 2 == 2
        assert 5 / 2.0 == 2.5
        assert 5.0 / 2 == 2.5
        """;

    AsmiTestUtil.assertNoErrors(source);
  }

}
