package xland.mcmod.enchlevellangpatch.impl;

import org.intellij.lang.annotations.MagicConstant;

import java.util.Locale;

final class Debug {
    private Debug() {}

    private static final String KEY_PREFIX = "langpatch.debug.";
    static final String KEY_NO_CACHE_ROMAN = KEY_PREFIX + "nocache.roman";
    static final String KEY_NO_CACHE_CHINESE = KEY_PREFIX + "nocache.chinese";
    static final String KEY_CACHE_CHECK = KEY_PREFIX + "cachecheck";
    // hash(default), disabled, calc
    static final int CACHE_CHECK_HASH = 0;
    static final int CACHE_CHECK_DISABLED = 1;
    static final int CACHE_CHECK_CALC = 2;

    static final boolean NO_CACHE_ROMAN = Boolean.getBoolean(KEY_NO_CACHE_ROMAN);
    static final boolean NO_CACHE_CHINESE = Boolean.getBoolean(KEY_NO_CACHE_CHINESE);
    @MagicConstant(intValues = {CACHE_CHECK_HASH, CACHE_CHECK_DISABLED, CACHE_CHECK_CALC})
    static final int CACHE_CHECK;

    static {
        switch (System.getProperty(KEY_CACHE_CHECK, "").toLowerCase(Locale.ROOT)) {
            case "disabled":
                CACHE_CHECK = CACHE_CHECK_DISABLED;
                break;
            case "calc":
                CACHE_CHECK = CACHE_CHECK_CALC;
                break;
            case "hash":
            default:
                CACHE_CHECK = CACHE_CHECK_HASH;
                break;
        }
    }
}
