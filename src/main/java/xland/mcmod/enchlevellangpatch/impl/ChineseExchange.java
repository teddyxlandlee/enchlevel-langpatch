package xland.mcmod.enchlevellangpatch.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

class ChineseExchange {
    // Chinese position
    private static final String[] POS = new String[]{"", "十", "百", "千"};
    // Chinese number
    private static final String[] NUM = new String[]{"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    // Chinese section position
    private static final String[] SEC = new String[]{"", "万", "亿", "万亿"};

    public static @NotNull String numberToChinese(@Range(from = 0, to = Integer.MAX_VALUE) int num) {
        if (num == 0) {
            return "零";
        }
        int sectionPosition = 0;
        StringBuilder ret = new StringBuilder();
        StringBuilder oneSection; // each section
        while (num > 0) {
            int section = num % 10000; // get the last section first (from low to high)
            oneSection = eachSection(section);
            if (section != 0) {
                oneSection.append(SEC[sectionPosition]);
            }
            num /= 10000;
            ret.insert(0, oneSection);
            sectionPosition++;
        }
        if ('零' == ret.charAt(0)) {
            ret.deleteCharAt(0);
        }
        if ("一十".equals(ret.substring(0, 2)))
            ret.deleteCharAt(0);    // fix: 一十 -> 十
        return ret.toString();
    }

    /**
     * Each section
     */
    private static StringBuilder eachSection(int num) {
        StringBuilder ret = new StringBuilder();
        boolean zero = true;
        for (int i = 0; i < 4; i++) { // 4 in 1 section, from low to high
            int end = num % 10;
            if (end == 0) {
                if (!zero) {
                    zero = true;
                    ret.append(NUM[0]);
                }
            } else {
                zero = false;
                ret.append(NUM[end]).append(POS[i]);
            }
            num = num / 10;
        }
        return ret.reverse();
    }

}
