package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;

import java.util.Map;
import java.util.Objects;

@API(status = API.Status.INTERNAL)
public final class IndependentLangPatchRegistry {
    private final BiMap<NamespacedKey, EnchantmentLevelLangPatch> map = HashBiMap.create();

    private final NamespacedKey defaultId;
    private EnchantmentLevelLangPatch defaultValue;

    private volatile ImmutableBiMap<String, EnchantmentLevelLangPatch> asImmutableBiMap;
    private boolean isFrozen;
    private final @NotNull String registryName;

    public static final NamespacedKey LP_DEFAULT = NamespacedKey.of("enchlevel-langpatch:default");

    IndependentLangPatchRegistry(@NotNull String registryName, NamespacedKey defaultId) {
        this.registryName = registryName;
        this.defaultId = defaultId;
    }

    @Contract("_ -> new")
    static @NotNull IndependentLangPatchRegistry of(String registryName) {
        return new IndependentLangPatchRegistry(registryName, LP_DEFAULT);
    }

    synchronized
    public void add(NamespacedKey id, EnchantmentLevelLangPatch e) {
        checkFreeze();
        map.put(id, e);
        if (Objects.equals(defaultId, id)) {
            this.defaultValue = e;
        }
        asImmutableBiMap = null;
    }

    public void add(String id, EnchantmentLevelLangPatch e) {
        this.add(NamespacedKey.of(id), e);
    }

    synchronized
    public EnchantmentLevelLangPatch get(NamespacedKey id) {
        return map.getOrDefault(id, defaultValue);
    }

    synchronized
    public NamespacedKey getId(EnchantmentLevelLangPatch e) {
        return map.inverse().getOrDefault(e, defaultId);
    }

    public NamespacedKey getDefaultId() {
        return defaultId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndependentLangPatchRegistry that = (IndependentLangPatchRegistry) o;
        return Objects.equals(map, that.map) && Objects.equals(getDefaultId(), that.getDefaultId())
                && Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, defaultId, defaultValue);
    }

    @Override
    public String toString() {
        return "Registry for ".concat(registryName);
    }

    private void checkFreeze() {
        if (isFrozen)
            throw new IllegalStateException("Registry is locked");
    }

    synchronized
    public boolean isFrozen() {
        return isFrozen;
    }

    synchronized
    public void freeze() {
        this.isFrozen = true;
    }

    @SuppressWarnings("unused")
    synchronized
    public void unfreeze() {
        this.isFrozen = false;
    }

    @SuppressWarnings("all")
    private ImmutableBiMap<String, EnchantmentLevelLangPatch> computeMap() {
        return this.map.entrySet().parallelStream()
                .collect(ImmutableBiMap.toImmutableBiMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }

    synchronized
    public ImmutableBiMap<String, EnchantmentLevelLangPatch> asImmutableBiMap() {
        ImmutableBiMap<String, EnchantmentLevelLangPatch> m;
        if ((m = this.asImmutableBiMap) == null) {
            this.asImmutableBiMap = m = computeMap();
        }
        return m;
    }
}
