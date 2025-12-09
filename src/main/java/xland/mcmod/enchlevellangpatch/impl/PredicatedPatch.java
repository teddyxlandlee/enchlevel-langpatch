package xland.mcmod.enchlevellangpatch.impl;

import org.jetbrains.annotations.NotNull;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;

import java.util.Objects;
import java.util.function.Predicate;

final class PredicatedPatch {
    private final @NotNull Predicate<String> keyPredicate;
    private final @NotNull EnchantmentLevelLangPatch langPatch;

    PredicatedPatch(@NotNull Predicate<String> keyPredicate, @NotNull EnchantmentLevelLangPatch langPatch) {
        Objects.requireNonNull(keyPredicate, "keyPredicate");
        Objects.requireNonNull(langPatch, "langPatch");

        this.keyPredicate = keyPredicate;
        this.langPatch = langPatch;
    }

    public @NotNull Predicate<String> getKeyPredicate() {
        return keyPredicate;
    }

    public @NotNull EnchantmentLevelLangPatch getLangPatch() {
        return langPatch;
    }

    // no need for equals() and hashCode()
}
