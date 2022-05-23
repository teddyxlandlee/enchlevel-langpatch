package xland.mcmod.enchlevellangpatch.impl;

import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    static @NotNull String intToRomanImpl(int i) {
        String ret = intToRoman(i);
        if (ret == null)
            return Integer.toString(i);
        return ret;
    }
}
