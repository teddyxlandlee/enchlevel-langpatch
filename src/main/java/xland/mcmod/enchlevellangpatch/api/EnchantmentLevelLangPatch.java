package xland.mcmod.enchlevellangpatch.api;

import org.apiguardian.api.API;
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
     *                    apply {@code edition} to the corresponding
     *                    language item.
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
     * @return {@code null} if {@code num} is out of range.
     */
    @Nullable @SuppressWarnings("unused")
    static String intToRoman(@Range(from = 1, to = 3998) int num) {
        return NumberFormatUtil.intToRoman(num);
    }

    /**
     * Provides an algorithm for roman-to-int translation.
     */
    @API(status = API.Status.DEPRECATED, since = "1.2")
    @Deprecated//forRemoval=true
    @ApiStatus.ScheduledForRemoval(inVersion = "1.3")
    static int romanToInt(@NotNull String s) {
        return NumberFormatUtil.romanToInt(Objects.requireNonNull(s));
    }

    /**
     * Register an extra rendering syntax for enchantment levels. <br />
     * Won't be applied without an extension library that invokes
     * {@link EnchantmentLevelLangPatchConfig#setCurrentEnchantmentHooks},
     * which modifies current enchantment level patch.
     *
     * @deprecated use {@link #registerEnchantmentPatch(String, EnchantmentLevelLangPatch)}
     * for better compatibility across versions.
     * This method will be removed in 1.3.0.
     */
    @Deprecated//forRemoval=true
    @API(status = API.Status.DEPRECATED, since = "1")
    @ApiStatus.ScheduledForRemoval(inVersion = "1.3")
    static void registerEnchantmentPatch(@NotNull net.minecraft.class_2960 id, @NotNull EnchantmentLevelLangPatch edition) {
        registerPotionPatch(id.toString(), edition);
    }

    /**
     * Register an extra rendering syntax for enchantment levels. <br />
     * Won't be applied without an extension library that invokes
     * {@link EnchantmentLevelLangPatchConfig#setCurrentEnchantmentHooks},
     * which modifies current enchantment level patch.
     *
     * @deprecated use {@link #registerEnchantmentPatch(String, EnchantmentLevelLangPatch)}
     * for better compatibility across versions.
     * This method will be removed in 1.3.0.
     */
    @Deprecated//forRemoval=true
    @API(status = API.Status.DEPRECATED, since = "1")
    @ApiStatus.ScheduledForRemoval(inVersion = "1.3")
    static void registerEnchantmentPatch(@NotNull net.minecraft.util.ResourceLocation id, @NotNull EnchantmentLevelLangPatch edition) {
        registerPotionPatch(id.toString(), edition);
    }

    /**
     * Register an extra rendering syntax for enchantment levels. <br />
     * Won't be applied without an extension library that invokes
     * {@link EnchantmentLevelLangPatchConfig#setCurrentEnchantmentHooks},
     * which modifies current enchantment level patch.
     *
     * @deprecated use {@link #registerEnchantmentPatch(String, EnchantmentLevelLangPatch)}
     * for better compatibility across versions.
     * This method will be removed in 1.3.0.
     */
    @Deprecated//forRemoval=true
    @API(status = API.Status.DEPRECATED, since = "1")
    @ApiStatus.ScheduledForRemoval(inVersion = "1.3")
    static void registerEnchantmentPatch(@NotNull net.minecraft.resources.ResourceLocation id, @NotNull EnchantmentLevelLangPatch edition) {
        registerPotionPatch(id.toString(), edition);
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
     */
    @SuppressWarnings("unused")
    static void registerEnchantmentPatch(
            @NotNull String id,
            @NotNull EnchantmentLevelLangPatch edition) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(edition, "patch");
        LangPatchImpl.hookPatch(NamespacedKey.of(id), edition, true);
    }

    /**
     * Register an extra rendering syntax for potion potency. <br />
     * Won't be applied without an extension library that invokes
     * {@link EnchantmentLevelLangPatchConfig#setCurrentPotionHooks}
     * which modifies current potion potency patch.
     *
     * @deprecated use {@link #registerPotionPatch(String, EnchantmentLevelLangPatch)}
     * for better compatibility across versions.
     * This method will be removed in 1.3.0.
     */
    @Deprecated//forRemoval=true
    @API(status = API.Status.DEPRECATED, since = "1")
    @ApiStatus.ScheduledForRemoval(inVersion = "1.3")
    static void registerPotionPatch(@NotNull net.minecraft.util.ResourceLocation id, @NotNull EnchantmentLevelLangPatch edition) {
        registerPotionPatch(id.toString(), edition);
    }

    /**
     * Register an extra rendering syntax for potion potency. <br />
     * Won't be applied without an extension library that invokes
     * {@link EnchantmentLevelLangPatchConfig#setCurrentPotionHooks}
     * which modifies current potion potency patch.
     *
     * @deprecated use {@link #registerPotionPatch(String, EnchantmentLevelLangPatch)}
     * for better compatibility across versions.
     * This method will be removed in 1.3.0.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.3")
    @Deprecated//forRemoval=true
    @API(status = API.Status.DEPRECATED, since = "1")
    static void registerPotionPatch(@NotNull net.minecraft.resources.ResourceLocation id, @NotNull EnchantmentLevelLangPatch edition) {
        registerPotionPatch(id.toString(), edition);
    }

    /**
     * Register an extra rendering syntax for potion potency. <br />
     * Won't be applied without an extension library that invokes
     * {@link EnchantmentLevelLangPatchConfig#setCurrentPotionHooks}
     * which modifies current potion potency patch.
     *
     * @deprecated use {@link #registerPotionPatch(String, EnchantmentLevelLangPatch)}
     * for better compatibility across versions.
     * This method will be removed in 1.3.0.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.3")
    @Deprecated//forRemoval=true
    @API(status = API.Status.DEPRECATED, since = "1")
    static void registerPotionPatch(@NotNull net.minecraft.class_2960 id, @NotNull EnchantmentLevelLangPatch edition) {
        registerPotionPatch(id.toString(), edition);
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
     */
    @SuppressWarnings("unused")
    static void registerPotionPatch(
            @NotNull String id,
            @NotNull EnchantmentLevelLangPatch edition) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(edition, "patch");
        LangPatchImpl.hookPatch(NamespacedKey.of(id), edition, false);
    }

    /**
     * The function for {@link EnchantmentLevelLangPatch}
     *
     * @param translationStorage an unmodifiable wrapping of
     *                          the current key-translation map
     * @param key the provided translation key when this
     * {@link EnchantmentLevelLangPatch patch} is applied.
     *
     * @return the translation (value) you modify.
     * @apiNote param 1 is {@link Map}, not
     * {@link com.google.common.collect.ImmutableMap}.
     * We use aggressive way to prevent memory issues, which may cause
     * compatibility issues with 1.0-mods.
     */
    String apply(@Unmodifiable Map<String, String> translationStorage, String key);
}
