package sh.sidd.asmi.e2e;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AssertTests {

  @Test
  public void shouldNotThrowOnAssertTrue() {
    Assertions.assertDoesNotThrow(() -> AsmiTestUtil.runSource("assert true"));
    Assertions.assertDoesNotThrow(() -> AsmiTestUtil.runSource("assert 1"));
  }

  @Test
  public void shouldThrowOnAssertFalse() {
    Assertions.assertThrows(AssertionError.class, () -> AsmiTestUtil.runSource("assert false"));
    Assertions.assertThrows(AssertionError.class, () -> AsmiTestUtil.runSource("assert 0"));
  }
}
