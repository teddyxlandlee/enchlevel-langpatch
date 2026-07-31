package xland.mcmod.enchlevellangpatch.test;

public class ValueTableValidationTest {
    public static void main(String[] args) {
        try {
            Class.forName("xland.mcmod.enchlevellangpatch.impl.ValueTableHolder");
        } catch (Throwable e) {
            throw new RuntimeException("Failed to load or initialize ValueTableHolder", e);
        }
    }
}
