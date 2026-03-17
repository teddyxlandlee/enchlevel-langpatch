package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@API(status = API.Status.INTERNAL)
public final class IndependentLangPatchRegistry implements Serializable {
    private final BiMap<NamespacedKey, EnchantmentLevelLangPatch> map = HashBiMap.create();

    private volatile transient ImmutableBiMap<String, EnchantmentLevelLangPatch> snapshot;

    private volatile boolean isFrozen;
    private transient ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final NamespacedKey defaultId;
    private transient EnchantmentLevelLangPatch defaultValue;
    private final @NotNull String registryName;

    public static final NamespacedKey LP_DEFAULT = NamespacedKey.of("enchlevel-langpatch:default");

    IndependentLangPatchRegistry(@NotNull String registryName, NamespacedKey defaultId) {
        Objects.requireNonNull(registryName, "registryName");
        this.registryName = registryName;
        this.defaultId = defaultId;
    }

    @Contract("_ -> new")
    static @NotNull IndependentLangPatchRegistry of(String registryName) {
        return new IndependentLangPatchRegistry(registryName, LP_DEFAULT);
    }

    public void add(NamespacedKey id, EnchantmentLevelLangPatch e) {
        readWriteLock.writeLock().lock();
        try {
            checkFreeze();
            map.put(id, e);
            if (Objects.equals(defaultId, id)) {
                this.defaultValue = e;
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
        // snapshot shall be nonnull
        return snapshot.getOrDefault(id.toString(), defaultValue);
    }

    public NamespacedKey getId(EnchantmentLevelLangPatch e) {
        if (isFrozen) return getIdCached(e);

        readWriteLock.readLock().lock();
        try {
            if (isFrozen) return getIdCached(e);
            return map.inverse().getOrDefault(e, defaultId);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private NamespacedKey getIdCached(EnchantmentLevelLangPatch e) {
        // snapshot shall be nonnull
        String s = snapshot.inverse().get(e);
        return s == null ? defaultId : NamespacedKey.of(s);
    }

    public NamespacedKey getDefaultId() {
        return defaultId;
    }

    public EnchantmentLevelLangPatch remove(NamespacedKey key) {
        readWriteLock.writeLock().lock();

        try {
            checkFreeze();
            this.snapshot = null;   // This is unnecessary, but let's do this
            EnchantmentLevelLangPatch oldValue = this.map.remove(key);
            if (oldValue != null && Objects.equals(defaultId, key)) defaultValue = null;
            return oldValue;
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
        return this.map.entrySet().stream()
                .collect(ImmutableBiMap.toImmutableBiMap(e -> e.getKey().toString(), Map.Entry::getValue));
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

    private static final long serialVersionUID = 1L;

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        this.readWriteLock = new ReentrantReadWriteLock();

        if (isFrozen) {
            snapshot = computeMap();
        }
        this.defaultValue = defaultId == null ? null : map.get(this.defaultId);    // If not found, set to null
    }
}
