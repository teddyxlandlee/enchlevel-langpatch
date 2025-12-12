package cpw.mods.modlauncher.api;

import org.jetbrains.annotations.NotNull;

public final class TypesafeMap {
    public static final class Key<T> implements Comparable<Key<T>> {
        @Override
        public int compareTo(@NotNull TypesafeMap.Key<T> o) {
            throw new AssertionError("Trying to invoke a stub method");
        }
    }
}
