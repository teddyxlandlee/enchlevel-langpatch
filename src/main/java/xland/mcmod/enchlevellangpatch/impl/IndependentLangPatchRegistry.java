package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.Identifier;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;

import java.util.Objects;

/** @see net.minecraft.util.registry.DefaultedRegistry */
@API(status = API.Status.INTERNAL)
public class IndependentLangPatchRegistry {
    private final BiMap<Identifier, EnchantmentLevelLangPatch> map
            = HashBiMap.create();
    private final Identifier defaultId;
    private EnchantmentLevelLangPatch defaultValue;

    IndependentLangPatchRegistry(Identifier defaultId) {
        this.defaultId = defaultId;
    }

    @Contract(" -> new")
    static @NotNull IndependentLangPatchRegistry of() {
        return new IndependentLangPatchRegistry(new Identifier("enchlevel-langpatch:default"));
    }

    public void add(Identifier id, EnchantmentLevelLangPatch e) {
        map.put(id, e);
        if (Objects.equals(defaultId, id)) {
            this.defaultValue = e;
        }
    }

    public void add(String id, EnchantmentLevelLangPatch e) {
        this.add(new Identifier(id), e);
    }

    public EnchantmentLevelLangPatch get(Identifier id) {
        return map.getOrDefault(id, defaultValue);
    }

    public Identifier getId(EnchantmentLevelLangPatch e) {
        return map.inverse().getOrDefault(e, defaultId);
    }

    public Identifier getDefaultId() {
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
}
