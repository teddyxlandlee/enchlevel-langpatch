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

//** @see net.minecraft.core.DefaultedRegistry */
@API(status = API.Status.INTERNAL)
public final class IndependentLangPatchRegistry {
    private final BiMap<NamespacedKey, EnchantmentLevelLangPatch> map
            = HashBiMap.create();
    private final NamespacedKey defaultId;
    private EnchantmentLevelLangPatch defaultValue;
    public static final NamespacedKey LP_DEFAULT = NamespacedKey.of("enchlevel-langpatch:default");

    IndependentLangPatchRegistry(NamespacedKey defaultId) {
        this.defaultId = defaultId;
    }

    @Contract(" -> new")
    static @NotNull IndependentLangPatchRegistry of() {
        return new IndependentLangPatchRegistry(LP_DEFAULT);
    }

    synchronized
    public void add(NamespacedKey id, EnchantmentLevelLangPatch e) {
        map.put(id, e);
        if (Objects.equals(defaultId, id)) {
            this.defaultValue = e;
        }
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
        return "IndependentLangPatchRegistry{" +
                "map=" + map +
                ", defaultId=" + defaultId +
                '}';
    }

    public ImmutableBiMap<String, EnchantmentLevelLangPatch> asImmutableBiMap() {
        //return ImmutableBiMap.copyOf(this.map)
        return this.map.entrySet().parallelStream()
                .collect(ImmutableBiMap.toImmutableBiMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }
}
