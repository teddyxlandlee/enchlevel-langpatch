package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.errorprone.annotations.ThreadSafe;
import org.apiguardian.api.API;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@API(status = API.Status.INTERNAL)
@ThreadSafe
public final class IndependentLangPatchRegistry implements Serializable {
    private final BiMap<NamespacedKey, EnchantmentLevelLangPatch> map = HashBiMap.create();

    private volatile transient @Nullable ImmutableBiMap<String, EnchantmentLevelLangPatch> snapshot;

    private volatile boolean isFrozen;
    private transient ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private transient EnchantmentLevelLangPatch defaultValue;
    private final String registryName;

    public static final NamespacedKey DEFAULT_KEY = NamespacedKey.of("enchlevel-langpatch:default");

    IndependentLangPatchRegistry(String registryName, EnchantmentLevelLangPatch defaultValue) {
        Objects.requireNonNull(registryName, "registryName");
        Objects.requireNonNull(defaultValue, "defaultValue");
        this.registryName = registryName;

        map.put(DEFAULT_KEY, defaultValue);
        this.defaultValue = defaultValue;
    }

    public void add(NamespacedKey id, EnchantmentLevelLangPatch patch) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(patch, "patch");
        readWriteLock.writeLock().lock();
        try {
            checkFreeze();
            map.put(id, patch);
            if (DEFAULT_KEY.equals(id)) {
                this.defaultValue = patch;
            }
            snapshot = null;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public void add(String id, EnchantmentLevelLangPatch e) {
        this.add(NamespacedKey.of(id), e);
    }

    public EnchantmentLevelLangPatch get(NamespacedKey id) {
        Objects.requireNonNull(id, "id");
        if (isFrozen) return getCached(id);

        readWriteLock.readLock().lock();
        try {
            if (isFrozen) return getCached(id);
            return map.getOrDefault(id, defaultValue);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private EnchantmentLevelLangPatch getCached(NamespacedKey id) {
        return Objects.requireNonNull(snapshot).getOrDefault(id.toString(), defaultValue);
    }

    public NamespacedKey getId(@Nullable EnchantmentLevelLangPatch e) {
        if (e == null) return DEFAULT_KEY;
        if (isFrozen) return getIdCached(e);

        readWriteLock.readLock().lock();
        try {
            if (isFrozen) return getIdCached(e);
            return map.inverse().getOrDefault(e, DEFAULT_KEY);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private NamespacedKey getIdCached(EnchantmentLevelLangPatch e) {
        // snapshot shall be nonnull
        String s = Objects.requireNonNull(snapshot).inverse().get(e);
        return s == null ? DEFAULT_KEY : NamespacedKey.of(s);
    }

    @ApiStatus.Experimental
    public @Nullable EnchantmentLevelLangPatch remove(NamespacedKey key) {
        if (DEFAULT_KEY.equals(key)) {
            throw new IllegalArgumentException("Default entry cannot be removed. Call add() to replace it.");
        }
        readWriteLock.writeLock().lock();

        try {
            checkFreeze();
            this.snapshot = null;
            return this.map.remove(key);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public String toString() {
        return "LPRegistry[" + registryName + ']';
    }

    private void checkFreeze() {
        if (isFrozen)
            throw new IllegalStateException(this + " is locked");
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public void freeze() {
        readWriteLock.writeLock().lock();
        try {
            if (isFrozen) return;
            snapshot = computeMap();
            this.isFrozen = true;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @ApiStatus.Experimental
    public void unfreeze() {
        readWriteLock.writeLock().lock();
        try {
            this.isFrozen = false;
            this.snapshot = null;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private ImmutableBiMap<String, EnchantmentLevelLangPatch> computeMap() {
        return this.map.entrySet().stream().collect(ImmutableBiMap.toImmutableBiMap(
                e -> e.getKey().toString(),
                Map.Entry::getValue
        ));
    }

    public ImmutableBiMap<String, EnchantmentLevelLangPatch> asImmutableBiMap() {
        ImmutableBiMap<String, EnchantmentLevelLangPatch> m;
        if ((m = this.snapshot) == null) {
            readWriteLock.writeLock().lock();
            try {
                if ((m = this.snapshot) == null) {
                    m = this.snapshot = computeMap();
                }
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }
        return m;
    }

    private static final long serialVersionUID = 2L;

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        EnchantmentLevelLangPatch defaultValue;
        if ((defaultValue = map.get(DEFAULT_KEY)) == null) {
            throw new InvalidObjectException("default entry is absent");
        }

        this.readWriteLock = new ReentrantReadWriteLock();

        if (isFrozen) {
            snapshot = computeMap();
        }
        this.defaultValue = defaultValue;
    }
}
