package sh.sidd.asmi.e2e;

import org.junit.jupiter.api.Test;

public class BinaryExprTests {
    @Test
    public void shouldSupportLessThan() {
        AsmiTestUtil.assertNoErrors("assert 1 < 2");
        AsmiTestUtil.assertNoErrors("assert 0 < 1");
        AsmiTestUtil.assertRuntimeError("assert 2 < 1");
    }

    @Test
    public void shouldSupportLessThanEqual() {
        AsmiTestUtil.assertNoErrors("assert 1 <= 1");
        AsmiTestUtil.assertNoErrors("assert 0 <= 1");
        AsmiTestUtil.assertRuntimeError("assert 2 <= 1");
    }

    @Test
    public void shouldSupportGreaterThan() {
        AsmiTestUtil.assertNoErrors("assert 2 > 1");
        AsmiTestUtil.assertNoErrors("assert 1 > 0");
        AsmiTestUtil.assertRuntimeError("assert 1 > 2");
    }

    @Test
    public void shouldSupportGreaterThanEqual() {
        AsmiTestUtil.assertNoErrors("assert 2 >= 1");
        AsmiTestUtil.assertNoErrors("assert 1 >= 0");
        AsmiTestUtil.assertRuntimeError("assert 1 >= 2");
    }
}
