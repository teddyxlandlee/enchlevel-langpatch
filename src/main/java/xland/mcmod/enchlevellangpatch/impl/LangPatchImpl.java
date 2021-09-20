package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.serialization.Lifecycle;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatchConfig;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class LangPatchImpl {
    private LangPatchImpl() {}

    @Deprecated static final Logger LOGGER = LogManager.getLogger("Enchantment Level Language Patcher");

    private static final EnchantmentLevelLangPatch
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

    public static void init() {}


    static final RegistryKey<Registry<EnchantmentLevelLangPatch>>
        ENCHANTMENT_HOOK_KEY = RegistryKey.ofRegistry(new Identifier("enchlevel-langpatch", "enchantment_hook")),
        POTION_HOOK_KEY = RegistryKey.ofRegistry(new Identifier("enchlevel-langpatch", "potion_hook"));
    static public final DefaultedRegistry<EnchantmentLevelLangPatch>
        ENCHANTMENT_HOOK = new DefaultedRegistry<>(
                "enchlevel-langpatch:default",
                ENCHANTMENT_HOOK_KEY,
                Lifecycle.stable()),
        POTION_HOOK = new DefaultedRegistry<>(
                "enchlevel-langpatch:default",
                POTION_HOOK_KEY,
                Lifecycle.stable());

    static {
        Registry.register(ENCHANTMENT_HOOK, "enchlevel-langpatch:default", DEFAULT_ENCHANTMENT_HOOKS);
        Registry.register(POTION_HOOK, "enchlevel-langpatch:default", DEFAULT_POTION_HOOKS);

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
            @NotNull Identifier id,
            @NotNull EnchantmentLevelLangPatch hooks,
            boolean enchantmentOrPotion /*1: enchantment
                                          0: potion*/) {
        if (enchantmentOrPotion) { // enchantment
            Registry.register(ENCHANTMENT_HOOK, id, hooks);
        } else {                  // potion
            Registry.register(POTION_HOOK, id, hooks);
        }
    }

    private static final List<ImmutablePair<Predicate<String>,
                EnchantmentLevelLangPatch>> PREDICATES
            = Collections.synchronizedList(Lists.newArrayList());

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
