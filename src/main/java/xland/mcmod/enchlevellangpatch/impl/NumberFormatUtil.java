package xland.mcmod.enchlevellangpatch.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NumberFormatUtil {
    private NumberFormatUtil() {}

    static boolean isDigit(@NotNull CharSequence s, final int offset) {
        int li = s.length() - 1;
        if (li < offset) return false;
        int c = s.charAt(offset);
        if (c == '0') return li == offset;

        for (int idx = li; idx >= offset; idx--) {
            c = s.charAt(idx);
            if (c < '0' || c > '9') return false;
        }

        return true;
    }
    @Nullable
    public static String intToRoman(int num) {
        if (num <= 0 || num >= 3999) {
            return null;
        }
        // Since there are only 3,998 items, why don't we pre-cache them
        return ValueTableHolder.ROMAN[num];
    }

    static @NotNull String intToRomanImpl(int i, int type) {
        if (i < 0) return Integer.toString(i);
        if (type >= 0) {
            return ChineseExchange.numberToChinese(i, type);
        } else {
            String ret = intToRoman(i);
            return ret == null ? Integer.toString(i) : ret;
        }
    }
}
