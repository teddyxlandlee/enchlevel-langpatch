package xland.mcmod.enchlevellangpatch.impl;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;

import java.util.function.IntFunction;

final class NumResultCacheMap extends Int2ObjectLinkedOpenHashMap<String> {
    private static final int CAPACITY = 32767;  // 32K

    NumResultCacheMap() {
        super(1024);
    }

    private void checkOutOfBounds() {
        if (CAPACITY <= size)
            removeFirst();
    }

    String computeOrStop(int k, IntFunction<? extends String> fun) {
        String s;
        if ((s = get(k)) == null) {
            String t;
            if ((t = fun.apply(k)) != null) {
                put(k, t);
                checkOutOfBounds();
            }
        }
        return s;
    }

    @Override
    public String computeIfAbsent(int k, IntFunction<? extends String> mappingFunction) {
        String t = super.computeIfAbsent(k, mappingFunction);
        checkOutOfBounds();
        return t;
    }
}
