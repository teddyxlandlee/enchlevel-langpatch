package xland.mcmod.enchlevellangpatch.api;

import org.apiguardian.api.API;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.*;
import xland.mcmod.enchlevellangpatch.impl.LangPatchImpl;
import xland.mcmod.enchlevellangpatch.impl.NamespacedKey;
import xland.mcmod.enchlevellangpatch.impl.NumberFormatUtil;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * An API to patch the language file of Minecraft, resource packs and mods.
 *
 * @see #apply(Map, String)
 */
@API(status = API.Status.STABLE)
@FunctionalInterface
public interface EnchantmentLevelLangPatch {
    /**
     * Register language patch for any language item you want.
     *
     * @param keyPredicate Predicate for the language key. If {@code true},
     *                    {@code edition} will be applied to the
     *                    corresponding language item.
     * @param edition The patch for the corresponding language item
     *
     * @see #registerEnchantmentPatch(String, EnchantmentLevelLangPatch)
     * @see #registerPotionPatch(String, EnchantmentLevelLangPatch)
     */
    static void registerPatch(@NotNull Predicate<String> keyPredicate,
                              @NotNull EnchantmentLevelLangPatch edition) {
        LangPatchImpl.register(Objects.requireNonNull(keyPredicate), Objects.requireNonNull(edition));
    }

    /**
     * Provides an algorithm for int-to-roman translation.
     * Thanks youdiaodaxue16.
     *
     * @return The number in roman format, or {@code null} if
     * {@code num} is out of range ({@code 1..3998}).
     */
    @Nullable @SuppressWarnings("unused")
    static String intToRoman(@Range(from = 1, to = 3998) int num) {
        return NumberFormatUtil.intToRoman(num);
    }
    
    /**
     * <p>Register an extra rendering syntax for enchantment levels. </p>
     * <p>Won't be applied without invoking
     * {@link EnchantmentLevelLangPatchConfig#setCurrentEnchantmentHooks},
     * which switches the enchantment level patch to whichever you want.</p>
     *
     * @param id <a href="https://minecraft.wiki/w/Resource_location">
     *           Namespaced key</a> for your patch. Please follow the naming rule
     *           of namespaced keys.
     * @param edition Your patch for enchantment levels.
     *
     * @see EnchantmentLevelLangPatchConfig#setCurrentEnchantmentHooks
     *
     * @see #registerPatch
     * @see #registerPotionPatch
     */
    static void registerEnchantmentPatch(
            @NotNull @Pattern("^([0-9a-z_\\-]+:)?[0-9a-z_\\-/]+$") String id,
            @NotNull EnchantmentLevelLangPatch edition) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(edition, "patch");
        LangPatchImpl.hookEnchantmentPatch(NamespacedKey.of(id), edition);
    }

    /**
     * <p>Register an extra rendering syntax for potion potency. </p>
     * <p>Won't be applied without invoking
     * {@link EnchantmentLevelLangPatchConfig#setCurrentPotionHooks},
     * which switches the potion potency patch to whichever you want.</p>
     *
     * @param id <a href="https://minecraft.wiki/w/Resource_location">
     *           Namespaced key</a> for your patch. Please follow the naming rule
     *           of namespaced keys.
     * @param edition Your patch for potion potencies.
     *
     * @see EnchantmentLevelLangPatchConfig#setCurrentPotionHooks
     *
     * @see #registerPatch
     * @see #registerEnchantmentPatch
     */
    static void registerPotionPatch(
            @NotNull @Pattern("^([0-9a-z_\\-]+:)?[0-9a-z_\\-/]+$") String id,
            @NotNull EnchantmentLevelLangPatch edition) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(edition, "patch");
        LangPatchImpl.hookPotionPatch(NamespacedKey.of(id), edition);
    }

    /**
     * The entrypoint for {@link EnchantmentLevelLangPatch}.
     *
     * @param translationStorage an unmodifiable wrapping of
     *                          the current key-translation map
     * @param key the provided translation key when this
     * {@link EnchantmentLevelLangPatch patch} is applied.
     *
     * @return the translation (value) you modify. Returning null indicates remaining unchanged.
     * @see #apply(Map, String, String)
     */
    @Nullable String apply(@Unmodifiable Map<String, String> translationStorage, String key);

    /**
     * The entrypoint for {@link EnchantmentLevelLangPatch}, with fallback string given.
     *
     * @param translationStorage an unmodifiable wrapping of
     *                          the current key-translation map
     * @param key the provided translation key when this
     * {@link EnchantmentLevelLangPatch patch} is applied.
     * @param fallback the fallback translation provided.
     *
     * @return the translation (value) you modify. Returning null indicates remaining unchanged.
     *
     * @implNote <p>This method is invoked by Minecraft 1.19.4 or above, which always
     * gives a fallback that is defaulted to {@code key}. So if you want to override
     * this method, please check whether {@code key} is equal to {@code fallback}.</p>
     * <p>In addition, {@link #apply(Map, String)} should be implemented as well even
     * if your mod is not designed for 1.19.3 or older versions.</p>
     *
     * @see #apply(Map, String)
     */
    @SuppressWarnings("unused")
    default @Nullable String apply(@Unmodifiable Map<String, String> translationStorage, String key, String fallback) {
        return apply(translationStorage, key);
    }

    @FunctionalInterface
    interface WithFallback extends EnchantmentLevelLangPatch {
        @Nullable
        default String apply(@Unmodifiable Map<String, String> translationStorage, String key) {
            return null;
        }

        @Override
        @Nullable
        String apply(@Unmodifiable Map<String, String> translationStorage, String key, String fallback);
    }

    /**
     * <p>Creates an {@link EnchantmentLevelLangPatch} that receives a fallback string.</p>
     * <p>This should be used when your patch is <b>only</b> applied in Minecraft 1.19.4+,
     * as the patch will be <b>ignored</b> when the fallback string is absent.</p>
     *
     * Example: <blockquote><pre>
     *     EnchantmentLevelLangPatch.registerPatch(
     *         key -> key.startsWith("example.prefix."),
     *         EnchantmentLevelLangPatch.withFallback((storage, key, fallback) -> Example.hook(key, fallback))
     *     )
     * </pre></blockquote>
     */
    @SuppressWarnings("unused")
    static EnchantmentLevelLangPatch withFallback(WithFallback patch) {
        return patch;
    }
}
