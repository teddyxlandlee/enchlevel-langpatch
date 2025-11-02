package xland.mcmod.enchlevellangpatch.test;

import org.apache.commons.lang3.Validate;
import xland.mcmod.enchlevellangpatch.impl.ChineseExchange;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public final class ChineseExchangeSpeedTest {
    public static void testCacheless() throws Throwable {
        elapseTime(() -> {
            for (int x = 1; x <= 10_000; x++) {
                for (int cc = 0; cc < 100; cc++) {
                    ChineseExchange.numberToChineseCacheless(x, 0);
                }
            }
        }, "1 -> 10,000 [cacheless]", 10_000L);
        elapseTime(() -> {
            for (int x = 10_001; x <= 65_536; x++) {
                for (int cc = 0; cc < 100; cc++) {
                    ChineseExchange.numberToChineseCacheless(x, 0);
                }
            }
        }, "10,001 -> 65,536 [cacheless]", 65_536L - 10_001L + 1);
        elapseTime(() -> {
            for (int x = 65_537; x <= 1_000_000; x++) {
                for (int cc = 0; cc < 100; cc++) {
                    ChineseExchange.numberToChineseCacheless(x, 0);
                }
            }
        }, "65,537 -> 1,000,000 [cacheless]", 1_000_000L - 65_536L + 1);
    }

    public static void testCached() throws Throwable {
        elapseTime(() -> {
            for (int x = 1; x <= 10_000; x++) {
                for (int cc = 0; cc < 100; cc++) {
                    ChineseExchange.numberToChinese(x, 0);
                }
                if (!Objects.equals(ChineseExchange.numberToChinese(x, 0), ChineseExchange.numberToChineseCacheless(x, 0))) {
                    throw new IllegalStateException("Wrong cache map for " + x);
                }
            }
        }, "1 -> 10,000 [cached]", 10_000L);
        elapseTime(() -> {
            for (int x = 10_001; x <= 65_536; x++) {
                for (int cc = 0; cc < 100; cc++) {
                    ChineseExchange.numberToChinese(x, 0);
                }
                if (!Objects.equals(ChineseExchange.numberToChinese(x, 0), ChineseExchange.numberToChineseCacheless(x, 0))) {
                    throw new IllegalStateException("Wrong cache map for " + x);
                }
            }
        }, "10,001 -> 65,536 [cached]", 65_536L - 10_001L + 1);
        elapseTime(() -> {
            for (int x = 65_537; x <= 1_000_000; x++) {
                for (int cc = 0; cc < 100; cc++) {
                    ChineseExchange.numberToChinese(x, 0);
                }
                if (!Objects.equals(ChineseExchange.numberToChinese(x, 0), ChineseExchange.numberToChineseCacheless(x, 0))) {
                    throw new IllegalStateException("Wrong cache map for " + x);
                }
            }
        }, "65,537 -> 1,000,000 [cached]", 1_000_000L - 65_536L + 1);
    }

    public static void test255() throws Throwable {
        elapseTime(() -> {
            Random rng = new Random(-4959108356860759198L);
            for (int cc = 0; cc < 100_000; cc++) {
                int x = rng.nextInt(256);
                ChineseExchange.numberToChinese(x, 0);
            }
        }, "test255() [cached]", 100_000);

        elapseTime(() -> {
            Random rng = new Random(-4959108356860759198L);
            for (int cc = 0; cc < 100_000; cc++) {
                int x = rng.nextInt(256);
                ChineseExchange.numberToChineseCacheless(x, 0);
            }
        }, "test255() [cacheless]", 100_000);
    }

    public static void test255Map() throws Throwable {
        Validate.notNull(PrecalculatedMap.MAP); // load the map
        elapseTime(() -> {
            Random rng = new Random(-4959108356860759198L);
            for (int cc = 0; cc < 100_000; cc++) {
                int x = rng.nextInt(256);
                String ignore = PrecalculatedMap.MAP[x];
            }
        }, "test255Map()", 100_000);
    }

    public static void main(String[] args) throws Throwable {
        elapseTime(() -> Validate.notNull(PrecalculatedMap.MAP), "load MAP", 1L);
        test255Map();
        System.out.println("\n================\n");
        test255();
        System.out.println("\n================\n");
        testCacheless();
        System.out.println();
        testCached();
        System.out.println("\n================\n");
        testCacheless();
        System.out.println();
        testCached();
        System.out.println("\n================\n");
        test255();
        System.out.println("\n================\n");
        test255Map();
    }

    private static void elapseTime(Task task, Object obj, long multiplier) throws Throwable {
        long startTime = System.nanoTime();
        task.doTask();
        final long durationNanos = System.nanoTime() - startTime;
        System.out.printf(
                Locale.SIMPLIFIED_CHINESE, "Time elapsed (%s): %,d nanos (%,.3f nanos avg)%n",
                obj, durationNanos, ((double)durationNanos / (double)multiplier)
        );
    }

    @FunctionalInterface
    interface Task {
        void doTask() throws Throwable;
    }

    static final class PrecalculatedMap {
        static final String[] MAP = new String[256];
        static {
            for (int x = 0; x < 256; x++) {
                MAP[x] = ChineseExchange.numberToChineseCacheless(x, 0);
            }
        }
    }
}
