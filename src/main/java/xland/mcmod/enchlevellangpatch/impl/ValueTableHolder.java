package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import org.jetbrains.annotations.NotNullByDefault;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@NotNullByDefault
@SuppressWarnings("UnstableApiUsage")
final class ValueTableHolder {
    static final String[] ROMAN = new String[3999];
    static final String[] CHINESE = new String[2 * 256];
    private ValueTableHolder() {}

    static {
        final HashCode expectedHash = HashCode.fromString("3c09cc78904fc47fd583b680ecfa9e2ad7370787ea149d843a56fb8f8c15c8d4");
        final InputStream inputStream = Objects.requireNonNull(
                ValueTableHolder.class.getResourceAsStream("ValueTable.txt"),
                "ValueTable.txt not found. This should not happen."
        );

        final HashingInputStream hashingInputStream = new HashingInputStream(Hashing.sha256(), inputStream);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(hashingInputStream, StandardCharsets.UTF_8))) {
            for (int x = 0; x < 3999; x++) ROMAN[x] = reader.readLine();
            for (int x = 0; x < 2 * 256; x++) CHINESE[x] = reader.readLine();
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }

        final HashCode hash = hashingInputStream.hash();
        if (!expectedHash.equals(hash)) {
            throw new IllegalStateException("ValueTable.txt is corrupted, sha256 hash mismatch: expected" + expectedHash + ", got " + hash);
        }
    }
}
