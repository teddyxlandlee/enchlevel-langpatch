package xland.mcmod.enchlevellangpatch.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NumberFormatUtil {
    static boolean isDigit(@NotNull String s, final int offset) {
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

    private static final NumResultCacheMap CACHE = new NumResultCacheMap();

    private static final int[] I_ARR = {1000,900,500,400,100,90,50,40,10,9,5,4,1};
    private static final String[] S_ARR = {"M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"};

    @Nullable
    public static String intToRoman(int num) {
        return CACHE.computeIfAbsent(num, NumberFormatUtil::intToRoman0);
    }

    private static String intToRoman0(int num) { // thanks youdiaodaxue16
        StringBuilder res = new StringBuilder();
        if(num <= 0 || num >= 3999){
            return null;
        }
        for(int i = 0;i < I_ARR.length;i++){
            int temp=num/I_ARR[i];
            while(temp > 0){
                res.append(S_ARR[i]);
                temp--;
            }
            num = num % I_ARR[i];
        }
        return res.toString();
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
