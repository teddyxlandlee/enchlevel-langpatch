package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class LangPatchImpl {
    private LangPatchImpl() {}

    private static final Logger LOGGER = LogManager.getLogger("Enchantment Level Language Patcher");

    private static final BiFunction<ImmutableMap<String, String>, String, String>
        DEFAULT_ENCHANTMENT_HOOKS = (ImmutableMap<String, String> translationStorage, String key) -> {
            if (translationStorage.containsKey(key)) return translationStorage.get(key);
            int lvl = Integer.parseInt(key.substring(18));
            return String.format(translationStorage.getOrDefault("enchantment.level.x", "enchantment.level.x"), lvl);
        },
        DEFAULT_POTION_HOOKS = (ImmutableMap<String, String> translationStorage, String key) -> {
            if (translationStorage.containsKey(key)) return translationStorage.get(key);
            int lvl = Integer.parseInt(key.substring(15)) + 1;  // Level 2 is III
            return String.format(translationStorage.getOrDefault("potion.potency.x", "potion.potency.x"), lvl);
        };

    private static @Nullable BiFunction<ImmutableMap<String, String>, String, String>
        ENCHANTMENT_HOOKS = null, POTION_HOOKS = null;

    public static void init() {}


    static {
        EnchantmentLevelLangPatch.registerPatch(
                s -> s.startsWith("enchantment.level.") && NumberFormatUtil.isDigit(s.substring(18)),
                (ImmutableMap<String, String> translationStorage, String key) ->
                        ENCHANTMENT_HOOKS == null ? DEFAULT_ENCHANTMENT_HOOKS.apply(translationStorage, key)
                                : ENCHANTMENT_HOOKS.apply(translationStorage, key)
        );
        EnchantmentLevelLangPatch.registerPatch(
                s -> s.startsWith("potion.potency.") && NumberFormatUtil.isDigit(s.substring(15)),
                (translationStorage, key) ->
                        POTION_HOOKS == null ? DEFAULT_POTION_HOOKS.apply(translationStorage, key)
                                : POTION_HOOKS.apply(translationStorage, key)
        );
    }

    public static @Nullable BiFunction<ImmutableMap<String, String>, String, String> hookPatch
            (@NotNull BiFunction<ImmutableMap<String, String>, String, String> hooks,
             boolean enchantmentOrPotion /*1: enchantment
                                           0: potion*/) {
        BiFunction<ImmutableMap<String, String>, String, String> biFunction;
        if (enchantmentOrPotion) { // enchantment
            biFunction = ENCHANTMENT_HOOKS;
            ENCHANTMENT_HOOKS = hooks;
        } else {                  // potion
            biFunction = POTION_HOOKS;
            POTION_HOOKS = hooks;
        }
        return biFunction;
    }

    private static final List<ImmutablePair<Predicate<String>,
                BiFunction<ImmutableMap<String, String>, String, String>>> PREDICATES
            = Collections.synchronizedList(Lists.newArrayList());

    public static void register(Predicate<String> keyPredicate,
                                BiFunction<ImmutableMap<String, String>, String, String> edition) {
        PREDICATES.add(ImmutablePair.of(keyPredicate, edition));
    }

    public static void forEach(BiFunction<? super Predicate<String>,
            ? super BiFunction<ImmutableMap<String, String>, String, String>, Boolean> biConsumer) {
        //PREDICATES.forEach(pair -> biConsumer.accept(pair.getLeft(), pair.getRight()));
        synchronized (PREDICATES) {
            for (ImmutablePair<Predicate<String>, BiFunction<ImmutableMap<String, String>, String, String>>
                    pair : PREDICATES) {
                if (biConsumer.apply(pair.getLeft(), pair.getRight())) return;
            }
        }
    }

}
