package sh.sidd.asmi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AssertTests {

  @Test
  public void shouldNotThrowOnAssertTrue() {
    Assertions.assertDoesNotThrow(() -> TestUtil.runSource("assert true"));
    Assertions.assertDoesNotThrow(() -> TestUtil.runSource("assert 1"));
  }

  @Test
  public void shouldThrowOnAssertFalse() {
    Assertions.assertThrows(AssertionError.class, () -> TestUtil.runSource("assert false"));
    Assertions.assertThrows(AssertionError.class, () -> TestUtil.runSource("assert 0"));
  }
}
