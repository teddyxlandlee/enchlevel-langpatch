package xland.mcmod.enchlevellangpatch.impl;

import org.jetbrains.annotations.Nullable;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;

import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public final class AsmHook {
    public static @Nullable String langPatchHookWithFallback(
            String key, Map<String, String> translations,
            String fallback
    ) {
        return langPatchHook(key, translations, fallback, true);
    }

    public static @Nullable String langPatchHook(
            String key,
            Map<String, String> translations) {
        return langPatchHook(key, translations, null, false);
    }

    private static @Nullable String langPatchHook(
            String key, Map<String, String> translations,
            String fallback, boolean useFallback
    ) {
//        Mutable<@Nullable String> ms = new MutableObject<>();
        String[] ret = new String[1];   // ret[0] == null

        /*
        * Post-1.16 versions report the `storage` field already unmodifiable.
        *   [20w22a+ uses Guava's ImmutableMap; 24w33a+ uses Map.copyOf()]
        * 1.13.2~20w21a uses a mutable hashmap. We can optimize it.
        *
        * Optimization:
        * If version is <1.16-alpha.20.22.a, we wrap Collections.unmodifiableMap() at
        *   invocation point *in bytecode*.
        * Otherwise, we just pass through the argument.
        *
        * Fun fact: `ne.mi.cl.re.la.ClientLanguage` (moj-name, `TranslationStorage` in yarn)
        *   was named as `Locale` (`ne.mi.cl.re.la` in moj-name, `ne.mi.cl.re` in srg-name)
        *   until 20w22a.
        */
        // assert translations == Map.copyOf(translations) ||
        //          translations instanceof ImmutableMap ||
        //          translations == Collections.unmodifiableMap(translations)

        final Map<String, String> unmodifiable = translations;
        LangPatchImpl.forEach((Predicate<String> keyPredicate,
                               EnchantmentLevelLangPatch valueMapping) -> {
            if (keyPredicate.test(key)) {
                String candidate;
                if (useFallback) {
                    candidate = valueMapping.apply(unmodifiable, key, fallback);
                } else {
                    candidate = valueMapping.apply(unmodifiable, key);
                }
                if (candidate == null) return false;    // user skip

//                ms.setValue(candidate);
                ret[0] = candidate;
                return true;    // interrupt
            }
            return false;   // predicate fail, skip
        });
//        return ms.getValue();
        return ret[0];
    }

    private AsmHook() {}
}
