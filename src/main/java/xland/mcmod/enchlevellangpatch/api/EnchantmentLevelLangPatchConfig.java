package xland.mcmod.enchlevellangpatch.api;

import com.google.common.collect.BiMap;
import net.minecraft.resources.ResourceLocation;
import org.apiguardian.api.API;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import xland.mcmod.enchlevellangpatch.impl.LangPatchImpl;

/**
 * An API to modify the current enchantment level patch and the current
 * potion potency patch.
 *
 * @see EnchantmentLevelLangPatch#registerEnchantmentPatch
 * @see EnchantmentLevelLangPatch#registerPotionPatch
 */
@API(status = API.Status.STABLE)
public class EnchantmentLevelLangPatchConfig {
    /**
     * A thread-safe storage for the ID of the current enchantment level and potion
     * potency patches.
     */
    static volatile @NotNull ResourceLocation
            currentEnchantmentHooksId = new ResourceLocation("enchlevel-langpatch:default"),
            currentPotionHooksId = new ResourceLocation("enchlevel-langpatch:default");

    /**
     * The setter for the current enchantment level patch. Will be set to default if
     * {@code hooks} is not registered.
     *
     * @see EnchantmentLevelLangPatch#registerEnchantmentPatch
     */
    @SuppressWarnings("unused")
    public static void setCurrentEnchantmentHooks(@Nullable EnchantmentLevelLangPatch hooks) {
        currentEnchantmentHooksId = LangPatchImpl.ENCHANTMENT_HOOK.getId(hooks);
    }

    /**
     * The setter for the current potion potency patch. Will be set to default if
     * {@code hooks} is not registered.
     *
     * @see EnchantmentLevelLangPatch#registerPotionPatch
     */
    @SuppressWarnings("unused")
    public static void setCurrentPotionHooks(@Nullable EnchantmentLevelLangPatch hooks) {
        currentPotionHooksId = LangPatchImpl.POTION_HOOK.getId(hooks);
    }

    /**
     * The getter for the current enchantment level patch. Will return default if
     * the corresponding ID is not registered.
     */
    public static EnchantmentLevelLangPatch getCurrentEnchantmentHooks() {
        return LangPatchImpl.ENCHANTMENT_HOOK.get(currentEnchantmentHooksId);
    }

    /**
     * The getter for the current potion potency patch. Will return default if
     * the corresponding ID is not registered.
     */
    public static EnchantmentLevelLangPatch getCurrentPotionHooks() {
        return LangPatchImpl.POTION_HOOK.get(currentPotionHooksId);
    }

    private EnchantmentLevelLangPatchConfig() {}

    /**
     * @return registered ID-LangPatch context of potion hooks.
     */
    @API(status = API.Status.EXPERIMENTAL)
    @Unmodifiable
    @SuppressWarnings("unused")
    public static BiMap<ResourceLocation, EnchantmentLevelLangPatch> getPotionHooksContext() {
        return LangPatchImpl.POTION_HOOK.asImmutableBiMap();
    }

    /**
     * @return registered ID-LangPatch context of enchantment hooks.
     */
    @API(status = API.Status.EXPERIMENTAL)
    @Unmodifiable
    @SuppressWarnings("unused")
    public static BiMap<ResourceLocation, EnchantmentLevelLangPatch> getEnchantmentHooksContext() {
        return LangPatchImpl.ENCHANTMENT_HOOK.asImmutableBiMap();
    }
}
