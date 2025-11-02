package xland.mcmod.enchlevellangpatch.test;

import xland.mcmod.enchlevellangpatch.impl.NumberFormatUtil;

public final class RomanTableAccessor {
    public static void test() {
        long startTime = System.nanoTime();
        System.out.println("roman(2333) = " + NumberFormatUtil.intToRoman(2333));
        long elapsedTime = System.nanoTime() - startTime;
        System.out.printf("Elapsed time: %,dns%n", elapsedTime);
    }

    public static void main(String[] args) {
        test();
        System.out.println("\n==============\n");
        test();
        System.out.println("\n==============\n");
        test();
        System.out.println("\n==============\n");
        test();
    }
}
