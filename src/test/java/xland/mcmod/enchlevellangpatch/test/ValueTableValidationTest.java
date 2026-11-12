package xland.mcmod.enchlevellangpatch.test;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class ValueTableValidationTest {
    @Test
    public void verifyTable() throws Exception {
        Class<?> c = Class.forName("xland.mcmod.enchlevellangpatch.impl.ValueTableHolder");
        Method verifier = c.getDeclaredMethod("verifyValues");
        verifier.setAccessible(true);
        verifier.invoke(null);
    }
}
