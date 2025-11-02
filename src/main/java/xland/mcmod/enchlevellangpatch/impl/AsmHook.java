package xland.mcmod.enchlevellangpatch.impl;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

@API(status = API.Status.EXPERIMENTAL)
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
        Mutable<@Nullable String> ms = new MutableObject<>();
        final Map<String, String> unmodifiable = Collections.unmodifiableMap(translations);
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

                ms.setValue(candidate);
                return true;    // interrupt
            }
            return false;   // predicate fail, skip
        });
        return ms.getValue();
    }

    private AsmHook() {}
}
