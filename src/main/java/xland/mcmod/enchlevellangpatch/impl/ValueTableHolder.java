package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.base.Preconditions;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class ValueTableHolder {
    static final String[] ROMAN = new String[3999];
    static final String[] CHINESE = new String[2 * 256];
    private static final String HASH = "3c09cc78904fc47fd583b680ecfa9e2ad7370787ea149d843a56fb8f8c15c8d4";
    private ValueTableHolder() {}

    private static void init() {
        final InputStream inputStream = ValueTableHolder.class.getResourceAsStream("ValueTable.txt");
        if (inputStream == null) throw new IllegalStateException("ValueTable.txt not found. This should not happen.");

        try {
            if (Debug.CACHE_CHECK == Debug.CACHE_CHECK_DISABLED) {
                readFromStream(inputStream);
            } else if (Debug.CACHE_CHECK == Debug.CACHE_CHECK_CALC) {
                readFromStream(inputStream);
                verifyValues();
            } else {
                @SuppressWarnings("UnstableApiUsage")
                final HashCode expectedHash = HashCode.fromString(HASH);
                @SuppressWarnings("UnstableApiUsage")
                final HashingInputStream hashingInputStream = new HashingInputStream(Hashing.sha256(), inputStream);

                readFromStream(hashingInputStream);

                @SuppressWarnings("UnstableApiUsage")
                final HashCode hash = hashingInputStream.hash();
                if (!Objects.equals(expectedHash, hash)) {
                    throw new IllegalStateException("ValueTable.txt is corrupted, sha256 hash mismatch: expected" + expectedHash + ", got " + hash);
                }
            }
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static void readFromStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            for (int x = 0; x < 3999; x++) ROMAN[x] = reader.readLine();
            for (int x = 0; x < 2 * 256; x++) CHINESE[x] = reader.readLine();
        }
    }

    @VisibleForTesting
    public static void verifyValues() throws IllegalStateException {
        Preconditions.checkState(ROMAN[0].isEmpty(), "!ROMAN[0].isEmpty()");
        for (int x = 1; x < 3999; x++) {
            Preconditions.checkState(
                    ROMAN[x].equals(NumberFormatUtil.intToRomanNoCache(x)),
                    "ROMAN[%s] mismatch", x
            );
        }
        for (int x = 0; x < 256; x++) {
            Preconditions.checkState(
                    CHINESE[x].equals(ChineseExchange.numberToChineseCacheless(x, ChineseExchange.NORMAL)),
                    "normal CHINESE[%s] mismatch", x
            );
        }
        for (int x = 0; x < 256; x++) {
            Preconditions.checkState(
                    CHINESE[x + 256].equals(ChineseExchange.numberToChineseCacheless(x, ChineseExchange.UPPER)),
                    "upper CHINESE[%s] mismatch", x
            );
        }
    }

    static {
        init();
    }
}
