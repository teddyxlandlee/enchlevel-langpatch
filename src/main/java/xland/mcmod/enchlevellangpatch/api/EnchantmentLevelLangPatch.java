package xland.mcmod.enchlevellangpatch.api;

import com.google.common.collect.ImmutableMap;
import org.apiguardian.api.API;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import xland.mcmod.enchlevellangpatch.impl.LangPatchImpl;
import xland.mcmod.enchlevellangpatch.impl.NumberFormatUtil;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@API(status = API.Status.STABLE)
public interface EnchantmentLevelLangPatch {
    static void registerPatch(@NotNull Predicate<String> keyPredicate,
                              @NotNull BiFunction<ImmutableMap<String, String>, String, String> edition) {
        LangPatchImpl.register(Objects.requireNonNull(keyPredicate), Objects.requireNonNull(edition));
    }

    @Nullable @SuppressWarnings("unused")
    static String intToRoman(@Range(from = 1, to = 3998) int num) { // thanks youdiaodaxue16
        return NumberFormatUtil.intToRoman(num);
    }

    @SuppressWarnings("unused")
    static int romanToInt(@NotNull String s) {
        return NumberFormatUtil.romanToInt(Objects.requireNonNull(s));
    }

    /*1: enchantment
      0: potion*/

    @SuppressWarnings("unused")
    static @Nullable BiFunction<ImmutableMap<String, String>, String, String> overrideEnchantmentPatch(
            @NotNull BiFunction<ImmutableMap<String, String>, String, String> edition) {
        return LangPatchImpl.hookPatch(Objects.requireNonNull(edition), true);
    }

    @SuppressWarnings("unused")
    static @Nullable BiFunction<ImmutableMap<String, String>, String, String> overridePotionPatch(
            @NotNull BiFunction<ImmutableMap<String, String>, String, String> edition) {
        return LangPatchImpl.hookPatch(Objects.requireNonNull(edition), false);
    }
}
