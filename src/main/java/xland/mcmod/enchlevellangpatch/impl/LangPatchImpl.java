package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatchConfig;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static xland.mcmod.enchlevellangpatch.impl.NumberFormatUtil.intToRomanImpl;

public final class LangPatchImpl {
    private LangPatchImpl() {}
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker MARKER = MarkerManager.getMarker("LangPatch/Impl");

    private static final List<ImmutablePair<Predicate<String>,
            EnchantmentLevelLangPatch>> PREDICATES
            = Collections.synchronizedList(Lists.newArrayList());

    private static final EnchantmentLevelLangPatch
        DEFAULT_ENCHANTMENT_HOOKS = (Map<String, String> translationStorage, String key) -> {
            String t = translationStorage.get(key);
            if (t != null) return t;

            int lvl = Integer.parseInt(key.substring(18));
            return ofRoman(translationStorage, lvl, "langpatch.conf.enchantment.default.type", "enchantment.level.x");
        },
        DEFAULT_POTION_HOOKS = (Map<String, String> translationStorage, String key) -> {
            String t = translationStorage.get(key);
            if (t != null) return t;

            int lvl = Integer.parseInt(key.substring(15)) + 1;  // Level 2 is III
            return ofRoman(translationStorage, lvl, "langpatch.conf.potion.default.type", "potion.potency.x");
        };
    private static String ofDefault(Map<String, String> translationStorage, int lvl, String key) {
        return String.format(translationStorage.getOrDefault(key, "%s"), lvl);
    }

    // To avoid same-check
    private static final EnchantmentLevelLangPatch
        ROMAN_ENCHANTMENT_HOOKS = DEFAULT_ENCHANTMENT_HOOKS::apply,
        ROMAN_POTION_HOOKS = DEFAULT_POTION_HOOKS::apply;
    private static String ofRoman(Map<String, String> translationStorage, int lvl, String configKey, String formatKey) {
        int cnMode;
        switch (translationStorage.getOrDefault(configKey, "").toLowerCase(Locale.ROOT)) {
            case "simplified":
            case "chinese":
            case "zh_normal":
                cnMode = ChineseExchange.NORMAL;
                break;

            case "traditional":
            case "zh_upper":
                cnMode = ChineseExchange.UPPER;
                break;

            case "numeral":
            case "number":
            case "numeric":
            case "arabic":
            case "":        // Community Voted: NUMERAL
            case "null":    // Community Voted: NUMERAL
            case "default": // Community Voted: NUMERAL
                return ofDefault(translationStorage, lvl, formatKey);

            case "no":
            case "roman":
            default:
                cnMode = -1;
        }

        return String.format(translationStorage.getOrDefault(formatKey, "%s"),
                intToRomanImpl(lvl, cnMode));
    }

    public static void init() {
        applyConf4();
        lockAll();
        EnchantmentLevelLangPatch.registerPatch(
                s -> s.startsWith("enchantment.level.") && NumberFormatUtil.isDigit(s, 18),
                (translationStorage, key) -> EnchantmentLevelLangPatchConfig
                        .getCurrentEnchantmentHooks()
                        .apply(translationStorage, key)
        );
        EnchantmentLevelLangPatch.registerPatch(
                s -> s.startsWith("potion.potency.") && NumberFormatUtil.isDigit(s, 15),
                (translationStorage, key) -> EnchantmentLevelLangPatchConfig
                        .getCurrentPotionHooks()
                        .apply(translationStorage, key)
        );
    }


    static public final IndependentLangPatchRegistry
        ENCHANTMENT_HOOK = IndependentLangPatchRegistry.of(),
        POTION_HOOK = IndependentLangPatchRegistry.of();

    static {
        ENCHANTMENT_HOOK.add("enchlevel-langpatch:default", DEFAULT_ENCHANTMENT_HOOKS);
        POTION_HOOK.add("enchlevel-langpatch:default", DEFAULT_POTION_HOOKS);
        ENCHANTMENT_HOOK.add("enchlevel-langpatch:roman", ROMAN_ENCHANTMENT_HOOKS);
        POTION_HOOK.add("enchlevel-langpatch:roman", ROMAN_POTION_HOOKS);
    }

    private static void applyConf4() {
        Class<?> c;
        try {
            c = Class.forName("xland.mcmod.enchlevellangpatch.ext.conf4.LangPatchConfigHooks");
            if (!ConfigProvider.class.isAssignableFrom(c)) {
                LOGGER.warn(MARKER, "{} is not instance of ConfigProvider", c);
                return;
            }
        } catch (ClassNotFoundException e) {
            LOGGER.debug(MARKER, "Conf4 not detected");
            return;
        }
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        LOGGER.debug(MARKER, "Detected Conf4");

        ConfigProvider provider;
        try {
            final MethodHandle cons = lookup.findConstructor(c, MethodType.methodType(void.class));
            provider = (ConfigProvider) cons.invoke();
        } catch (Throwable t) {
            LOGGER.warn(MARKER, "Cannot instantiate {}", c, t);
            return;
        }

        LOGGER.debug(MARKER, "Applying Conf4");
        Marker marker = MarkerManager.getMarker("LangPatch/Conf4");
        final String e = provider.getEnchantmentConfig(), p = provider.getPotionConfig();
        if (e != null) {
            final EnchantmentLevelLangPatch patch = ENCHANTMENT_HOOK.get(NamespacedKey.of(e));
            EnchantmentLevelLangPatchConfig.setCurrentEnchantmentHooks(patch);
            LOGGER.info(marker, "Set enchantment hook to {}", e);
        }
        if (p != null) {
            final EnchantmentLevelLangPatch patch = POTION_HOOK.get(NamespacedKey.of(p));
            EnchantmentLevelLangPatchConfig.setCurrentPotionHooks(patch);
            LOGGER.info(marker, "Set potion hook to {}", e);
        }
    }

    private static void lockAll() {
        ENCHANTMENT_HOOK.freeze();
        POTION_HOOK.freeze();
        LOGGER.debug(MARKER, "Registries are locked");
    }

    public static void hookPatch(
            @NotNull NamespacedKey id,
            @NotNull EnchantmentLevelLangPatch hooks,
            boolean enchantmentOrPotion /*1: enchantment
                                          0: potion*/) {
        IndependentLangPatchRegistry reg = enchantmentOrPotion ? ENCHANTMENT_HOOK : POTION_HOOK;
        if (reg.isFrozen()) {
            LOGGER.warn(MARKER, "The registry is frozen. Changes may not be applied");
            return;
        }
        reg.add(id, hooks);
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
