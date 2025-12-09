package xland.mcmod.enchlevellangpatch.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.VisibleForTesting;

@VisibleForTesting
public final class ChineseExchange {
    private final NumResultCacheMap cacheMap = new NumResultCacheMap();
    private final String[] pos;
    private final char[] num;
    private final String[] sec;
    private final char zeroC;
    private final String zeroS;
    private final String tenOne;

    private static final class Lazy {
        static final ChineseExchange[] EXCHANGES = {
                new ChineseExchange(
                        new String[]{"", "十", "百", "千"},
                        "零一二三四五六七八九".toCharArray(),
                        new String[]{"", "万", "亿", "万亿"},
                        "零", "十一"
                ),
                new ChineseExchange(
                        new String[]{"", "拾", "佰", "仟"},
                        "零壹貳叄肆伍陸柒捌玖".toCharArray(),
                        new String[]{"", "萬", "億", "兆"},
                        "零", "拾壹"
                )
        };
    }

    static final int NORMAL = 0, UPPER = 1;

    ChineseExchange(String[] pos, char[] num, String[] sec, String zeroS, String tenOne) {
        this.pos = pos;
        this.num = num;
        this.sec = sec;
        this.zeroC = num[0];
        this.zeroS = zeroS;
        this.tenOne = tenOne;

//        assert tenOne.equals(pos[1] + num[1]) && zeroS.equals(String.valueOf(num[0]));
    }

    @VisibleForTesting
    public static @NotNull String numberToChinese(@Range(from = 0, to = Integer.MAX_VALUE) int num, int type) {
        if (num < 256) {
            return ValueTableHolder.CHINESE[type][num];
        }
        return Lazy.EXCHANGES[type].numberToChinese(num);
    }

    @VisibleForTesting
    public static String numberToChineseCacheless(int num, int type) {
        return Lazy.EXCHANGES[type].numberToChinese0(num);
    }

    String numberToChinese(int num) {
        return cacheMap.computeIfAbsent(num, this::numberToChinese0);
    }

    private @NotNull String numberToChinese0(int num) {
        if (num == 0) {
            return zeroS;
        }
        int sectionPosition = 0;
        StringBuilder ret = new StringBuilder();
        //StringBuilder oneSection; // each section
        while (num > 0) {
            int section = num % 10000; // get the last section first (from low to high)
            if (section != 0) {
                ret.append(this.sec[sectionPosition]);
            }
            eachSection(section, ret);
            num /= 10000;
            //ret.append(oneSection);
            sectionPosition++;
        }
        
        int i;
        if (zeroC == ret.charAt(i = ret.length() - 1)) {
            ret.setLength(i);
        }
        
        i = ret.length() - 2;
        if (i < 0)  // ret.length() < 2
            return ret.toString();
        
        if (tenOne.equals(ret.substring(i)))
            ret.setLength(i + 1);    // fix: 一十 -> 十
        return ret.reverse().toString();
    }

    private void eachSection(int num, StringBuilder ret) {
        //StringBuilder ret = new StringBuilder();
        boolean zero = true;    // if num == 0: ""
        for (int i = 0; i < 4; i++) { // 4 in 1 section, from low to high
            int end = num % 10;
            if (end == 0) {
                if (!zero) {
                    zero = true;
                    ret.append(zeroC);
                }
            } else {
                zero = false;
                ret.append(this.pos[i]).append(this.num[end]);
            }
            num /= 10;
        }
    }
}
