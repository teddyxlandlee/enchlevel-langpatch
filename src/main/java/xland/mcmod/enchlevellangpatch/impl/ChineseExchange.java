package xland.mcmod.enchlevellangpatch.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

class ChineseExchange {
    private static final NumResultCacheMap CACHE = new NumResultCacheMap();

    // Chinese position
    private static final String[] POS = new String[]{"", "十", "百", "千"};
    // Chinese number
    private static final String[] NUM = new String[]{"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    // Chinese section position
    private static final String[] SEC = new String[]{"", "万", "亿", "万亿"};

    public static @NotNull String numberToChinese(@Range(from = 0, to = Integer.MAX_VALUE) int num) {
        return CACHE.computeOrStop(num, ChineseExchange::numberToChinese0);
    }

    private static @NotNull String numberToChinese0(int num) {
        if (num == 0) {
            return "零";
        }
        int sectionPosition = 0;
        StringBuilder ret = new StringBuilder();
        //StringBuilder oneSection; // each section
        while (num > 0) {
            int section = num % 10000; // get the last section first (from low to high)
            if (section != 0) {
                ret.append(SEC[sectionPosition]);
            }
            eachSection(section, ret);
            num /= 10000;
            //ret.append(oneSection);
            sectionPosition++;
        }
        
        int i;
        if ('零' == ret.charAt(i = ret.length() - 1)) {
            ret.setLength(i);
        }
        
        i = ret.length() - 2;
        if (i < 0)  // ret.length() < 2
            return ret.toString();
        
        if ("十一".equals(ret.substring(i)))
            ret.setLength(++i);    // fix: 一十 -> 十
        return ret.reverse().toString();
    }

    /**
     * Each section
     */
    private static void eachSection(int num, StringBuilder ret) {
        //StringBuilder ret = new StringBuilder();
        boolean zero = true;    // if num == 0: ""
        for (int i = 0; i < 4; i++) { // 4 in 1 section, from low to high
            int end = num % 10;
            if (end == 0) {
                if (!zero) {
                    zero = true;
                    ret.append(NUM[0]);
                }
            } else {
                zero = false;
                ret.append(POS[i]).append(NUM[end]);
            }
            num /= 10;
        }
        //return ret.reverse();
    }

}
