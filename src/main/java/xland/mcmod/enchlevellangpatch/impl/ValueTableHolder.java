package xland.mcmod.enchlevellangpatch.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class ValueTableHolder {
    static final String[] ROMAN = new String[3999];
    static final String[][] CHINESE = new String[2][256];
    private ValueTableHolder() {}

    static {
        // sha256 3c09cc78904fc47fd583b680ecfa9e2ad7370787ea149d843a56fb8f8c15c8d4
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
                ValueTableHolder.class.getResourceAsStream("ValueTable.txt"),
                "ValueTable.txt not found. This should not happen."
        ), StandardCharsets.UTF_8))) {
            for (int x = 0; x < 3999; x++) ROMAN[x] = reader.readLine();
            for (int x = 0; x < 256; x++) CHINESE[0][x] = reader.readLine();
            for (int x = 0; x < 256; x++) CHINESE[1][x] = reader.readLine();
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
}
