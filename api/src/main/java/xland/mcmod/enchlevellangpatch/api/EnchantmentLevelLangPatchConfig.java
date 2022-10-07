package xland.mcmod.enchlevellangpatch.api;

import com.google.common.collect.BiMap;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

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
     * The setter for the current enchantment level patch. Will be set to default if
     * {@code hooks} is not registered.
     *
     * @see EnchantmentLevelLangPatch#registerEnchantmentPatch
     * @param hooks the patch you want to set. It should be registered with
     *              {@link EnchantmentLevelLangPatch#registerEnchantmentPatch}.
     */
    @SuppressWarnings("unused")
    public static void setCurrentEnchantmentHooks(@Nullable EnchantmentLevelLangPatch hooks) {
        throw new AssertionError("This is the API. See implementations in https://github.com/teddyxlandlee/enchlevel-langpatch.");
    }

    /**
     * The setter for the current potion potency patch. Will be set to default if
     * {@code hooks} is not registered.
     *
     * @see EnchantmentLevelLangPatch#registerPotionPatch
     * @param hooks the patch you want to set. It should be registered with
     *              {@link EnchantmentLevelLangPatch#registerPotionPatch}
     */
    @SuppressWarnings("unused")
    public static void setCurrentPotionHooks(@Nullable EnchantmentLevelLangPatch hooks) {
        throw new AssertionError("This is the API. See implementations in https://github.com/teddyxlandlee/enchlevel-langpatch.");
    }

    /**
     * The getter for the current enchantment level patch. Will return default if
     * the corresponding ID is not registered.
     * @return the current enchantment patch
     */
    @SuppressWarnings("unused")
    public static EnchantmentLevelLangPatch getCurrentEnchantmentHooks() {
        throw new AssertionError("This is the API. See implementations in https://github.com/teddyxlandlee/enchlevel-langpatch.");
    }

    /**
     * The getter for the current potion potency patch. Will return default if
     * the corresponding ID is not registered.
     * @return the current potion patch
     */
    @SuppressWarnings("unused")
    public static EnchantmentLevelLangPatch getCurrentPotionHooks() {
        throw new AssertionError("This is the API. See implementations in https://github.com/teddyxlandlee/enchlevel-langpatch.");
    }

    private EnchantmentLevelLangPatchConfig() {}

    /**
     * @return registered ID-LangPatch context of potion hooks.
     */
    @API(status = API.Status.EXPERIMENTAL)
    @Unmodifiable
    @SuppressWarnings("unused")
    public static BiMap<String, EnchantmentLevelLangPatch> getPotionHooksContext() {
        throw new AssertionError("This is the API. See implementations in https://github.com/teddyxlandlee/enchlevel-langpatch.");
    }

    /**
     * @return registered ID-LangPatch context of enchantment hooks.
     */
    @API(status = API.Status.EXPERIMENTAL)
    @Unmodifiable
    @SuppressWarnings("unused")
    public static BiMap<String, EnchantmentLevelLangPatch> getEnchantmentHooksContext() {
        throw new AssertionError("This is the API. See implementations in https://github.com/teddyxlandlee/enchlevel-langpatch.");
    }
}
