package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.resources.ResourceLocation;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;

import java.util.Objects;

/** @see net.minecraft.core.DefaultedRegistry */
@API(status = API.Status.INTERNAL)
public final class IndependentLangPatchRegistry {
    private final BiMap<ResourceLocation, EnchantmentLevelLangPatch> map
            = HashBiMap.create();
    private final ResourceLocation defaultId;
    private EnchantmentLevelLangPatch defaultValue;

    IndependentLangPatchRegistry(ResourceLocation defaultId) {
        this.defaultId = defaultId;
    }

    @Contract(" -> new")
    static @NotNull IndependentLangPatchRegistry of() {
        return new IndependentLangPatchRegistry(new ResourceLocation("enchlevel-langpatch:default"));
    }

    public void add(ResourceLocation id, EnchantmentLevelLangPatch e) {
        map.put(id, e);
        if (Objects.equals(defaultId, id)) {
            this.defaultValue = e;
        }
    }

    public void add(String id, EnchantmentLevelLangPatch e) {
        this.add(new ResourceLocation(id), e);
    }

    public EnchantmentLevelLangPatch get(ResourceLocation id) {
        return map.getOrDefault(id, defaultValue);
    }

    public ResourceLocation getId(EnchantmentLevelLangPatch e) {
        return map.inverse().getOrDefault(e, defaultId);
    }

    public ResourceLocation getDefaultId() {
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

    public ImmutableBiMap<ResourceLocation, EnchantmentLevelLangPatch> asImmutableBiMap() {
        return ImmutableBiMap.copyOf(this.map);
    }
}
