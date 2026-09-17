package xland.mcmod.enchlevellangpatch.impl;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;

import java.util.function.IntFunction;

final class NumResultCacheMap {
    private static final int CAPACITY = 32767;  // 32K
    private final Int2ObjectLinkedOpenHashMap<String> backingMap = new Int2ObjectLinkedOpenHashMap<>(1024);

    private void checkOutOfBounds() {
        if (CAPACITY <= backingMap.size()) backingMap.removeFirst();
    }

    public String computeIfAbsent(int k, IntFunction<? extends String> mappingFunction) {
        String t = backingMap.computeIfAbsent(k, mappingFunction);
        checkOutOfBounds();
        return t;
    }
}
