package xland.mcmod.enchlevellangpatch.test;

import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;
import xland.mcmod.enchlevellangpatch.impl.ChineseExchange;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class ValueTableGenerator {
    private static final String INDENT = "\040\040\040\040";
    private static final String DOUBLE_INDENT = INDENT + INDENT;
    private static final String TRIPLE_INDENT = DOUBLE_INDENT + INDENT;

    public static void writeFile(Appendable appendable, CharSequence packageName) throws IOException {
        appendable.append("// Auto-generated. Do not edit.\n");
        appendable.append("// Range: 0 | [1, 3998]\n");
        appendable.append("package ").append(packageName).append(";\n\n");
        appendable.append("final class ValueTableHolder {\n").append(INDENT);

        // ROMAN
        appendable.append("static final String[] ROMAN = {\n").append(DOUBLE_INDENT);
        appendable.append("\"\"");  // for ZERO
        for (int x = 1; x <= 3998; x++) {
            appendable.append(x % 10 == 0 ? ",\n" + DOUBLE_INDENT : ", ");
            appendable.append('"').append(EnchantmentLevelLangPatch.intToRoman(x)).append('"'); // no escapes
        }
        appendable.append('\n').append(INDENT).append("};\n\n"); // end of array definition

        // CHINESE
        appendable.append(INDENT);
        appendable.append("static final String[][] CHINESE = {\n").append(DOUBLE_INDENT).append("{\n").append(TRIPLE_INDENT);
        for (int x = 0; x < 256; x++) { // simplified
            String s = ChineseExchange.numberToChineseCacheless(x, 0);
            if (x != 0) {
                appendable.append(x % 10 == 0 ? ",\n" + TRIPLE_INDENT : ", ");
            }
            appendable.append('"').append(s).append('"');
        }
        appendable.append('\n').append(DOUBLE_INDENT).append("}, {\n").append(TRIPLE_INDENT);
        for (int x = 0; x < 256; x++) { // traditional
            String s = ChineseExchange.numberToChineseCacheless(x, 1);
            if (x != 0) {
                appendable.append(x % 10 == 0 ? ",\n" + TRIPLE_INDENT : ", ");
            }
            appendable.append('"').append(s).append('"');
        }
        appendable.append('\n').append(DOUBLE_INDENT).append("}\n").append(INDENT).append("};\n\n");

        appendable.append(INDENT).append("private ValueTableHolder() {}");
        appendable.append('\n').append('}');                // end of class definition
        appendable.append('\n');                            // newline
    }

    public static void main(String[] args) throws Throwable {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("/tmp/ValueTableHolder.java"))) {
            writeFile(writer, "xland.mcmod.enchlevellangpatch.impl");
        }
    }
}
