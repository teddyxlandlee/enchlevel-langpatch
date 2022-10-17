package xland.mctestmod.enchlevellangpatch.chinese;

import xland.mcmod.enchlevellangpatch.impl.$Accessor$$lRVJvigvEZhCYEyDjnrFF;

import java.util.function.Function;

public class TestChineseExchange {
    private static final Function<Integer, String> FUNC = $Accessor$$lRVJvigvEZhCYEyDjnrFF.FUNC;

    public static void main(String[] args) {
        System.out.println(FUNC.apply(114514));
        System.out.println(FUNC.apply(1919810));
        System.out.println(FUNC.apply(1_0000_0000));
    }
}
