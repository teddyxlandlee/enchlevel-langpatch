package xland.mcmod.enchlevellangpatch.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

enum TelemetryConfig {
    DISABLED,
    NECESSARY,
    FUNCTIONAL,
    OPTIONAL,
    ;

    private static final TelemetryConfig DEFAULT = DISABLED;
    private static final TelemetryConfig CURRENT = detect();

    public static TelemetryConfig getCurrent() {
        return CURRENT;
    }

    // will stop detecting (silent ignore) since 3.10
    private static TelemetryConfig detect() {
        if (Boolean.getBoolean("xland.mcmod.enchlevellangpatch.disableTelemetry")) {
            return DISABLED;
        }

        Path path = Paths.get("config", "enchlevel-langpatch-telemetry.txt");
        if (!Files.exists(path)) return DEFAULT;    // not using Files.notExists(): "unknown" -> false -> DEFAULT

        String line;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) break;
            }
            if (line == null) return DEFAULT;
        } catch (IOException e) {
            // fail silently
            return DEFAULT;
        }

        switch (line.toLowerCase(Locale.ROOT)) {
            case "disabled":
            case "-1":
                return DISABLED;
            case "necessary":
            case "0":
                return NECESSARY;
            case "functional":
            case "1":
                return FUNCTIONAL;
            case "optional":
            case "2":
                return OPTIONAL;
            default:
                return DEFAULT;
        }
    }
}
