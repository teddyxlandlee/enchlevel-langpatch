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
public class AsmHook {
    public static @Nullable String langPatchHookWithFallback(
            String key, Map<String, String> translations,
            String fallback
    ) {
        Mutable<@Nullable String> ms = new MutableObject<>();
        LangPatchImpl.forEach((Predicate<String> keyPredicate,
                               EnchantmentLevelLangPatch valueMapping) -> {
            if (keyPredicate.test(key)) {
                //cir.setReturnValue(valueMapping.apply(ImmutableMap.copyOf(translations), key));
                ms.setValue(valueMapping.apply(Collections.unmodifiableMap(translations), key));
                return true;
            } return false;
        });
        return ms.getValue();
    }

    public static @Nullable String langPatchHook(
            String key,
            Map<String, String> translations) {
        Mutable<@Nullable String> ms = new MutableObject<>();
        LangPatchImpl.forEach((Predicate<String> keyPredicate,
                               EnchantmentLevelLangPatch valueMapping) -> {
            if (keyPredicate.test(key)) {
                //cir.setReturnValue(valueMapping.apply(ImmutableMap.copyOf(translations), key));
                ms.setValue(valueMapping.apply(Collections.unmodifiableMap(translations), key));
                return true;
            } return false;
        });
        return ms.getValue();
    }
}
