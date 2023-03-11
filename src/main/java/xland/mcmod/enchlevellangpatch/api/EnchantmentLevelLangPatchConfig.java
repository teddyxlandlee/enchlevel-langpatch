package xland.mcmod.enchlevellangpatch.api;

import com.google.common.collect.BiMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apiguardian.api.API;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import xland.mcmod.enchlevellangpatch.impl.IndependentLangPatchRegistry;
import xland.mcmod.enchlevellangpatch.impl.LangPatchImpl;
import xland.mcmod.enchlevellangpatch.impl.NamespacedKey;

/**
 * An API to modify the current enchantment level patch and the current
 * potion potency patch.
 *
 * @see EnchantmentLevelLangPatch#registerEnchantmentPatch
 * @see EnchantmentLevelLangPatch#registerPotionPatch
 */
@API(status = API.Status.STABLE)
public class EnchantmentLevelLangPatchConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker MARKER = MarkerManager.getMarker("LangPatch/Config");

    /**
     * A thread-safe storage for the ID of the current enchantment level and potion
     * potency patches.
     */
    static volatile @NotNull NamespacedKey
            currentEnchantmentHooksId = IndependentLangPatchRegistry.LP_DEFAULT,
            currentPotionHooksId = IndependentLangPatchRegistry.LP_DEFAULT;

    /**
     * The setter for the current enchantment level patch. Will be set to default if
     * {@code hooks} is not registered.
     *
     * @see EnchantmentLevelLangPatch#registerEnchantmentPatch
     */
    @SuppressWarnings("unused")
    public static void setCurrentEnchantmentHooks(@Nullable EnchantmentLevelLangPatch hooks) {
        if (LangPatchImpl.ENCHANTMENT_HOOK.isFrozen()) {
            LOGGER.warn(MARKER, "Enchantment Hooks is frozen. Changes may not be applied.");
            return;
        }
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
        if (LangPatchImpl.ENCHANTMENT_HOOK.isFrozen()) {
            LOGGER.warn(MARKER, "Potion Hooks is frozen. Changes may not be applied.");
            return;
        }
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
    public static BiMap<String, EnchantmentLevelLangPatch> getPotionHooksContext() {
        return LangPatchImpl.POTION_HOOK.asImmutableBiMap();
    }

    /**
     * @return registered ID-LangPatch context of enchantment hooks.
     */
    @API(status = API.Status.EXPERIMENTAL)
    @Unmodifiable
    @SuppressWarnings("unused")
    public static BiMap<String, EnchantmentLevelLangPatch> getEnchantmentHooksContext() {
        return LangPatchImpl.ENCHANTMENT_HOOK.asImmutableBiMap();
    }
}
