package xland.mcmod.enchlevellangpatch.api;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.Identifier;
import org.apiguardian.api.API;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import xland.mcmod.enchlevellangpatch.impl.LangPatchImpl;
import xland.mcmod.enchlevellangpatch.impl.NumberFormatUtil;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * An API to patch the language file of Minecraft, resource packs and mods.
 *
 * @see #apply(ImmutableMap, String)
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
     * @see #registerEnchantmentPatch(Identifier, EnchantmentLevelLangPatch)
     * @see #registerPotionPatch(Identifier, EnchantmentLevelLangPatch)
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
    @SuppressWarnings("unused")
    static int romanToInt(@NotNull String s) {
        return NumberFormatUtil.romanToInt(Objects.requireNonNull(s));
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
            @NotNull Identifier id,
            @NotNull EnchantmentLevelLangPatch edition) {
        LangPatchImpl.hookPatch(Objects.requireNonNull(id),
                Objects.requireNonNull(edition), true);
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
            @NotNull Identifier id,
            @NotNull EnchantmentLevelLangPatch edition) {
        LangPatchImpl.hookPatch(Objects.requireNonNull(id),
                Objects.requireNonNull(edition), false);
    }

    /**
     * Registry identifier of enchantment hooks.
     * @since 0.3.1
     */
    Identifier ENCHANTMENT_HOOK_REGISTRY_ID = new Identifier("enchlevel-langpatch", "enchantment_hook");

    /**
     * Registry identifier of potion hooks.
     * @since 0.3.1
     */
    Identifier POTION_HOOK_REGISTRY_ID = new Identifier("enchlevel-langpatch", "potion_hook");

    /**
     * The function for {@link EnchantmentLevelLangPatch}
     *
     * @param translationStorage a copy of the current key-translation map
     * @param key the provided translation key when this
     * {@link EnchantmentLevelLangPatch patch} is applied.
     *
     * @return the translation (value) you modify.
     */
    String apply(ImmutableMap<String, String> translationStorage, String key);
}
