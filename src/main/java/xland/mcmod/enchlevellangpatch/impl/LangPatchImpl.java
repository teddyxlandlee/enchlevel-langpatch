package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatchConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static xland.mcmod.enchlevellangpatch.impl.NumberFormatUtil.intToRomanImpl;

public final class LangPatchImpl {
    private LangPatchImpl() {}

    private static final List<ImmutablePair<Predicate<String>,
            EnchantmentLevelLangPatch>> PREDICATES
            = Collections.synchronizedList(Lists.newArrayList());

    private static final EnchantmentLevelLangPatch
        DEFAULT_ENCHANTMENT_HOOKS = (Map<String, String> translationStorage, String key) -> {
            if (translationStorage.containsKey(key)) return translationStorage.get(key);
            int lvl = Integer.parseInt(key.substring(18));
            return String.format(translationStorage.getOrDefault("enchantment.level.x", "%s"), lvl);
        },
        DEFAULT_POTION_HOOKS = (Map<String, String> translationStorage, String key) -> {
            if (translationStorage.containsKey(key)) return translationStorage.get(key);
            int lvl = Integer.parseInt(key.substring(15)) + 1;  // Level 2 is III
            return String.format(translationStorage.getOrDefault("potion.potency.x", "%s"), lvl);
        };
    private static final EnchantmentLevelLangPatch
        ROMAN_ENCHANTMENT_HOOKS = (translationStorage, key) -> {
            if (translationStorage.containsKey(key)) return translationStorage.get(key);
            int lvl = Integer.parseInt(key.substring(18));
            return String.format(translationStorage.getOrDefault("enchantment.level.x", "%s"),
                    intToRomanImpl(lvl));
        },
        ROMAN_POTION_HOOKS = (Map<String, String> translationStorage, String key) -> {
            if (translationStorage.containsKey(key)) return translationStorage.get(key);
            int lvl = Integer.parseInt(key.substring(15)) + 1;  // Level 2 is III
            return String.format(translationStorage.getOrDefault("potion.potency.x", "%s"),
                    intToRomanImpl(lvl));
        };

    public static void init() {}


    static public final IndependentLangPatchRegistry
        ENCHANTMENT_HOOK = IndependentLangPatchRegistry.of(),
        POTION_HOOK = IndependentLangPatchRegistry.of();

    static {
        ENCHANTMENT_HOOK.add("enchlevel-langpatch:default", DEFAULT_ENCHANTMENT_HOOKS);
        POTION_HOOK.add("enchlevel-langpatch:default", DEFAULT_POTION_HOOKS);
        ENCHANTMENT_HOOK.add("enchlevel-langpatch:roman", ROMAN_ENCHANTMENT_HOOKS);
        POTION_HOOK.add("enchlevel-langpatch:roman", ROMAN_POTION_HOOKS);

        EnchantmentLevelLangPatch.registerPatch(
                s -> s.startsWith("enchantment.level.") && NumberFormatUtil.isDigit(s.substring(18)),
                //(translationStorage, key) ->
                (translationStorage, key) -> EnchantmentLevelLangPatchConfig
                        .getCurrentEnchantmentHooks()
                        .apply(translationStorage, key)
        );
        EnchantmentLevelLangPatch.registerPatch(
                s -> s.startsWith("potion.potency.") && NumberFormatUtil.isDigit(s.substring(15)),
                (translationStorage, key) -> EnchantmentLevelLangPatchConfig
                        .getCurrentPotionHooks()
                        .apply(translationStorage, key)
        );
    }

    public static void hookPatch(
            @NotNull ResourceLocation id,
            @NotNull EnchantmentLevelLangPatch hooks,
            boolean enchantmentOrPotion /*1: enchantment
                                          0: potion*/) {
        if (enchantmentOrPotion) { // enchantment
            ENCHANTMENT_HOOK.add(id, hooks);
        } else {                  // potion
            POTION_HOOK.add(id, hooks);
        }
    }

    public static void register(Predicate<String> keyPredicate,
                                EnchantmentLevelLangPatch edition) {
        PREDICATES.add(ImmutablePair.of(keyPredicate, edition));
    }

    public static void forEach(BiFunction<? super Predicate<String>,
                ? super EnchantmentLevelLangPatch, Boolean> biConsumer) {
        //PREDICATES.forEach(pair -> biConsumer.accept(pair.getLeft(), pair.getRight()));
        synchronized (PREDICATES) {
            for (ImmutablePair<Predicate<String>, EnchantmentLevelLangPatch>
                    pair : PREDICATES) {
                if (biConsumer.apply(pair.getLeft(), pair.getRight())) return;
            }
        }
    }

}
