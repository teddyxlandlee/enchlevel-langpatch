package xland.mcmod.enchlevellangpatch.api;

import org.apiguardian.api.API;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.*;

import java.util.Map;
import java.util.function.Predicate;

/**
 * An API to patch the language file of Minecraft, resource packs and mods.
 *
 * @see #apply(Map, String)
 */
@API(status = API.Status.STABLE)
@FunctionalInterface
@SuppressWarnings("unused")
public interface EnchantmentLevelLangPatch {
    /**
     * Register language patch for any language item you want.
     *
     * @param keyPredicate Predicate for the language key. If {@code true},
     *                    apply {@code edition} to the corresponding
     *                    language item.
     * @param edition The patch for the corresponding language item
     *
     * @see #registerEnchantmentPatch(String, EnchantmentLevelLangPatch)
     * @see #registerPotionPatch(String, EnchantmentLevelLangPatch)
     */
    static void registerPatch(@NotNull Predicate<String> keyPredicate,
                              @NotNull EnchantmentLevelLangPatch edition) {
        throw new AssertionError("This is the API. See implementations in https://github.com/teddyxlandlee/enchlevel-langpatch.");
    }

    /**
     * Provides an algorithm for int-to-roman translation.
     * Thanks youdiaodaxue16.
     * @param num the number you want to convert to roman
     *            format
     *
     * @return {@code null} if {@code num} is out of range.
     */
    @Nullable
    static String intToRoman(@Range(from = 1, to = 3998) int num) {
        throw new AssertionError("This is the API. See implementations in https://github.com/teddyxlandlee/enchlevel-langpatch.");
    }

    /**
     * Provides an algorithm for roman-to-int translation.
     * @param s the roman number that you want to convert
     *          to {@code int}.
     * @return the corresponding {@code int} value. If
     * {@code s} is invalid, then a wrong thing will be
     * returned.
     * @deprecated this method won't be used, and it does
     * not have a regex check, which would cause unknown
     * error.
     */
    @SuppressWarnings("unused")
    @Deprecated
    @API(status = API.Status.DEPRECATED, since = "1.2")
    @ApiStatus.ScheduledForRemoval(inVersion = "1.3")
    static int romanToInt(@NotNull String s) {
        throw new AssertionError("This is the API. See implementations in https://github.com/teddyxlandlee/enchlevel-langpatch.");
    }

    /**
     * Register an extra rendering syntax for enchantment levels. <br />
     * Won't be applied without an extension library that invokes
     * {@link EnchantmentLevelLangPatchConfig#setCurrentEnchantmentHooks},
     * which modifies current enchantment level patch.
     *
     * @see EnchantmentLevelLangPatchConfig#setCurrentEnchantmentHooks
     *
     * @see #registerPatch
     * @see #registerPotionPatch
     *
     * @param id a {@code ResourceLocation} of your patch. Must match the
     *           standard of a {@code ResourceLocation}:
     *           {@code ^([0-9a-z_\-]+:)?[0-9a-z_\-/]+$}
     * @param edition the patch
     */
    static void registerEnchantmentPatch(
            @NotNull @Pattern("^([0-9a-z_\\-]+:)?[0-9a-z_\\-/]+$") String id,
            @NotNull EnchantmentLevelLangPatch edition) {
        throw new AssertionError("This is the API. See implementations in https://github.com/teddyxlandlee/enchlevel-langpatch.");
    }
    
    /**
     * Register an extra rendering syntax for potion potency. <br />
     * Won't be applied without an extension library that invokes
     * {@link EnchantmentLevelLangPatchConfig#setCurrentPotionHooks}
     * which modifies current potion potency patch.
     *
     * @see EnchantmentLevelLangPatchConfig#setCurrentPotionHooks
     *
     * @see #registerPatch
     * @see #registerEnchantmentPatch
     *
     * @param id a {@code ResourceLocation} of your patch. Must match the
     *           standard of a {@code ResourceLocation}:
     *           {@code ^([0-9a-z_\-]+:)?[0-9a-z_\-/]+$}
     * @param edition the patch
     */
    static void registerPotionPatch(
            @NotNull String id,
            @NotNull EnchantmentLevelLangPatch edition) {
        throw new AssertionError("This is the API. See implementations in https://github.com/teddyxlandlee/enchlevel-langpatch.");
    }

    /**
     * The function for {@link EnchantmentLevelLangPatch}
     * <br>
     * <b>API Note:</b>&nbsp;param 1 is {@link Map}, not
     * {@link com.google.common.collect.ImmutableMap}.
     * We use aggressive way to prevent memory issues, which may cause
     * compatibility issues with 1.0-mods.
     *
     * @param translationStorage an unmodifiable wrapping of
     *                          the current key-translation map
     * @param key the provided translation key when this
     * {@link EnchantmentLevelLangPatch patch} is applied.
     *
     * @return the translation (value) you modify.
     *
     */
    String apply(@Unmodifiable Map<String, String> translationStorage, String key);
}
