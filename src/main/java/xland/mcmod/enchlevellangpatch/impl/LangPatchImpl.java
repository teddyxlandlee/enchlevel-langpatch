package xland.mcmod.enchlevellangpatch.impl;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatchConfig;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.function.Predicate;

import static xland.mcmod.enchlevellangpatch.impl.NumberFormatUtil.intToRomanImpl;

public final class LangPatchImpl {
    private LangPatchImpl() {}
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker MARKER = MarkerManager.getMarker("LangPatch/Impl");

    public static final String KEY_ENCHANTMENT_TYPE = "langpatch.conf.enchantment.default.type";
    public static final String KEY_ENCHANTMENT_OVERRIDE = "langpatch.conf.enchantment.override";
    public static final String KEY_ENCHANTMENT_FORMAT = "enchantment.level.x";

    public static final String KEY_POTION_TYPE = "langpatch.conf.potion.default.type";
    public static final String KEY_POTION_OVERRIDE = "langpatch.conf.potion.override";
    public static final String KEY_POTION_FORMAT = "potion.potency.x";

    // *** BUILT-IN REGISTRIES & HOOKS *** //
    private static final List<PredicatedPatch> PREDICATES = Collections.synchronizedList(new ArrayList<>());

    private static final EnchantmentLevelLangPatch DEFAULT_ENCHANTMENT_HOOKS =
            (Map<String, String> translationStorage, String key) -> {
                if (!Boolean.parseBoolean(translationStorage.get(KEY_ENCHANTMENT_OVERRIDE))) {
                    // Do not override given translation
                    String t = translationStorage.get(key);
                    if (t != null) return t;
                }

                int lvl = Integer.parseInt(key.substring(18));
                return configuredFormat(translationStorage, lvl, KEY_ENCHANTMENT_TYPE, KEY_ENCHANTMENT_FORMAT);
            };

    private static final EnchantmentLevelLangPatch DEFAULT_POTION_HOOKS =
            (Map<String, String> translationStorage, String key) -> {
                if (!Boolean.parseBoolean(translationStorage.get(KEY_POTION_OVERRIDE))) {
                    // Do not override given translation
                    String t = translationStorage.get(key);
                    if (t != null) return t;
                }

                int lvl = Integer.parseInt(key.substring(15)) + 1;  // Level 2 is III
                return configuredFormat(translationStorage, lvl, KEY_POTION_TYPE, KEY_POTION_FORMAT);
            };

    private static @NotNull String safeFormat(Map<String, String> translationStorage, String key, @NotNull Object arg) {
        try {
            return String.format(translationStorage.getOrDefault(key, "%s"), arg);
        } catch (IllegalFormatException e) {
            // Invalid format
            LOGGER.warn(MARKER, "Invalid format string for translation key {}. Using as-is format.", key, e);
            return arg.toString();
        }
    }

    // To avoid same-check
    @SuppressWarnings("FunctionalExpressionCanBeFolded")
    private static final EnchantmentLevelLangPatch ROMAN_ENCHANTMENT_HOOKS = DEFAULT_ENCHANTMENT_HOOKS::apply;

    @SuppressWarnings("FunctionalExpressionCanBeFolded")
    private static final EnchantmentLevelLangPatch ROMAN_POTION_HOOKS = DEFAULT_POTION_HOOKS::apply;

    private static @Nullable String configuredFormat(Map<String, String> translationStorage, int lvl, String configKey, String formatKey) {
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
                return safeFormat(translationStorage, formatKey, lvl);

            case "skip":
            case "skipped":
            case "ignore":
            case "ignored":
                return null;    // Return `null` to skip

            case "no":
            case "roman":
            default:
                cnMode = -1;
        }

        return safeFormat(translationStorage, formatKey, intToRomanImpl(lvl, cnMode));
    }

    static public final IndependentLangPatchRegistry
        ENCHANTMENT_HOOK = IndependentLangPatchRegistry.of("enchantments"),
        POTION_HOOK = IndependentLangPatchRegistry.of("potions");

    static {
        ENCHANTMENT_HOOK.add("enchlevel-langpatch:default", DEFAULT_ENCHANTMENT_HOOKS);
        POTION_HOOK.add("enchlevel-langpatch:default", DEFAULT_POTION_HOOKS);
        ENCHANTMENT_HOOK.add("enchlevel-langpatch:roman", ROMAN_ENCHANTMENT_HOOKS);
        POTION_HOOK.add("enchlevel-langpatch:roman", ROMAN_POTION_HOOKS);

        // preload roman/chinese map
        Validate.isTrue("I".equals(EnchantmentLevelLangPatch.intToRoman(1)));
    }

    // *** REGISTRY *** //

    public static void hookEnchantmentPatch(
            @NotNull NamespacedKey id,
            @NotNull EnchantmentLevelLangPatch hooks
    ) {
        hookPatch(ENCHANTMENT_HOOK, id, hooks);
    }

    public static void hookPotionPatch(
            @NotNull NamespacedKey id,
            @NotNull EnchantmentLevelLangPatch hooks
    ) {
        hookPatch(POTION_HOOK, id, hooks);
    }

    private static void hookPatch(
            IndependentLangPatchRegistry reg,
            NamespacedKey id,
            EnchantmentLevelLangPatch hooks
    ) {
        if (reg.isFrozen()) {
            LOGGER.warn(MARKER, "{} is frozen. Patch {} may not be applied", reg, id);
            return;
        }
        reg.add(id, hooks);
    }

    public static void register(Predicate<String> keyPredicate,
                                EnchantmentLevelLangPatch edition) {
        PREDICATES.add(new PredicatedPatch(keyPredicate, edition));
    }

    private static void lockAll() {
        ENCHANTMENT_HOOK.freeze();
        POTION_HOOK.freeze();
        LOGGER.debug(MARKER, "Registries are locked");
    }

    // *** ENTRYPOINT *** //

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

    // *** MINECRAFT HOOK *** //

    static void forEach(InterruptablePatchConsumer consumer) {
        synchronized (PREDICATES) {
            for (PredicatedPatch patch : PREDICATES) {
                if (consumer.interrupt(patch)) return;
            }
        }
    }

    @FunctionalInterface
    interface InterruptablePatchConsumer {
        boolean interrupt(Predicate<String> keyPredicate, EnchantmentLevelLangPatch langPatch);

        default boolean interrupt(PredicatedPatch patch) {
            return interrupt(patch.getKeyPredicate(), patch.getLangPatch());
        }
    }
}
