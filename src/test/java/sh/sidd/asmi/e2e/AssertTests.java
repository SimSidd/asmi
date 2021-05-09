package sh.sidd.asmi.e2e;

import org.junit.jupiter.api.Test;

public class AssertTests {

  @Test
  public void shouldNotThrowOnAssertTrue() {
    AsmiTestUtil.assertNoErrors("assert true");
    AsmiTestUtil.assertNoErrors("assert 1");
  }

  @Test
  public void shouldThrowOnAssertFalse() {
    AsmiTestUtil.assertRuntimeError("assert false", "assert false");
    AsmiTestUtil.assertRuntimeError("assert 0", "assert 0");
  }
}
