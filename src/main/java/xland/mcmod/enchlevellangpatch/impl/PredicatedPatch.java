package xland.mcmod.enchlevellangpatch.impl;

import org.jetbrains.annotations.NotNullByDefault;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;

import java.util.Objects;
import java.util.function.Predicate;

@NotNullByDefault
final class PredicatedPatch {
    private final Predicate<String> keyPredicate;
    private final EnchantmentLevelLangPatch langPatch;

    PredicatedPatch(Predicate<String> keyPredicate, EnchantmentLevelLangPatch langPatch) {
        Objects.requireNonNull(keyPredicate, "keyPredicate");
        Objects.requireNonNull(langPatch, "langPatch");

        this.keyPredicate = keyPredicate;
        this.langPatch = langPatch;
    }

    public Predicate<String> getKeyPredicate() {
        return keyPredicate;
    }

    public EnchantmentLevelLangPatch getLangPatch() {
        return langPatch;
    }

    // no need for equals() and hashCode()
}
