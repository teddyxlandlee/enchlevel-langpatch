package xland.mcmod.enchlevellangpatch.impl;

import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class NumberFormatUtil {
    static boolean isDigit(@NotNull String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException var) {
            return false;
        }
    }

    private static final int[] I_ARR = {1000,900,500,400,100,90,50,40,10,9,5,4,1};
    private static final String[] S_ARR = {"M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"};

    @Nullable
    public static String intToRoman(int num) { // thanks youdiaodaxue16
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

    private static final Char2IntArrayMap R2I = new Char2IntArrayMap(
            new char[] {'I', 'V', 'X', 'L', 'C', 'D', 'M'},
            new int [] {1,   5,   10,  50,  100, 500, 1000}
    );

    public static int romanToInt(@NotNull String s) {
        if(s.length() == 0){
            return 0;
        }
        int res = R2I.get(s.charAt(s.length() - 1)); // Strings ends with '\0'
        for(int i = s.length() - 2;i >= 0;i--){   // start from zero
            if(R2I.get(s.charAt(i)) >= R2I.get(s.charAt(i + 1))){
                res += R2I.get(s.charAt(i));
            }else{
                res -= R2I.get(s.charAt(i));
            }
        }
        return res;
    }

    static @NotNull String intToRomanImpl(int i, boolean chinese) {
        if (i < 0) return Integer.toString(i);
        if (chinese) {
            return ChineseExchange.numberToChinese(i);
        } else {
            String ret = intToRoman(i);
            return ret == null ? Integer.toString(i) : ret;
        }
    }
}

class ChineseExchange {
    //中文数字权位
    private static final String[] CHINESE_POSITION = new String[]{"", "十", "百", "千"};
    //中文数字位
    private static final String[] CHINESE_NUMBER = new String[]{"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private static final String[] CHINESE_SECTION_POSITION = new String[]{"", "万", "亿", "万亿"};

    /*
     * 阿拉伯数字转中文
     */
    public static @NotNull String numberToChinese(@Range(from = 0, to = Integer.MAX_VALUE) int num){
        if(num == 0){
            return "零";
        }
        int sectionPosition = 0;
        StringBuilder ret = new StringBuilder();
        StringBuilder oneSection; // each section
        while(num>0){
            int section = num%10000; // get the last section first (from low to high)
            oneSection = eachSection(section);
            if(section != 0){
                oneSection.append(CHINESE_SECTION_POSITION[sectionPosition]);
            }
            num = num / 10000;
            ret.insert(0, oneSection);
            sectionPosition++;
        }
        if('零' == ret.charAt(0)){
            ret.deleteCharAt(0);
        }
        if ("一十".equals(ret.substring(0, 2)))
            ret.deleteCharAt(0);    // fix: 一十 -> 十
        return ret.toString();
    }

    /**
     * Each section
     */
    private static StringBuilder eachSection(int num){
        StringBuilder ret = new StringBuilder();
        boolean zero = true;
        for(int i=0;i<4;i++){ // 4 in 1 section, from low to high
            int end = num%10;
            if(end == 0){
                if(!zero){
                    zero = true;
                    ret.append(CHINESE_NUMBER[0]);
                }
            }else{
                zero = false;
                ret.append(CHINESE_NUMBER[end]).append(CHINESE_POSITION[i]);
            }
            num = num/10;
        }
        return ret.reverse();
    }

}
