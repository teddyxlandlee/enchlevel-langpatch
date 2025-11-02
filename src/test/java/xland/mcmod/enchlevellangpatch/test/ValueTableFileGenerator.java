package xland.mcmod.enchlevellangpatch.test;

import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;
import xland.mcmod.enchlevellangpatch.impl.ChineseExchange;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class ValueTableFileGenerator {
    public static void generate(Appendable appendable) throws IOException {
        // ROMAN
        appendable.append('\n');    // 0: empty
        for (int x = 1; x <= 3998; x++) {
            appendable.append(EnchantmentLevelLangPatch.intToRoman(x)).append('\n');
        }

        // CHINESE
        generateChinese(appendable, 0);
        generateChinese(appendable, 1);
    }

    private static void generateChinese(Appendable appendable, int type) throws IOException {
        for (int x = 0; x < 256; x++) {
            appendable.append(ChineseExchange.numberToChineseCacheless(x, type)).append('\n');
        }
    }

    public static void main(String[] args) throws Throwable {
        // UTF-8
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("/tmp/ValueTable.txt"))) {
            generate(writer);
        }
    }
}
