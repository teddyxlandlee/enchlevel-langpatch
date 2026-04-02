package xland.mcmod.enchlevellangpatch.impl.telemetry;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public enum TelemetryConfig {
    DISABLED,
    MANDATORY,
    FULL,
    ;

    private static final TelemetryConfig CURRENT;

    public static TelemetryConfig getCurrent() {
        return CURRENT;
    }

    static {
        boolean isTelemetryDisabled0;
        boolean enablesFullTelemetry0 = false;

        if (Boolean.getBoolean("xland.mcmod.enchlevellangpatch.disableTelemetry")) {
            isTelemetryDisabled0 = true;
        } else {
            Path path = Paths.get("config", "enchlevel-langpatch-telemetry.txt");
            if (Files.notExists(path)) {
                isTelemetryDisabled0 = false;
            } else {
                try (BufferedReader reader = Files.newBufferedReader(path)) {
                    String option;
                    while ((option = reader.readLine()) != null) {
                        option = option.trim();
                        if (!option.isEmpty() && !option.startsWith("#")) break;
                    }

                    if (option == null) {
                        LangPatchTelemetry.LOGGER.warn("Invalid telemetry config file: {}", path);
                        isTelemetryDisabled0 = false;
                    } else {
                        switch (option.toLowerCase(Locale.ROOT)) {
                            case "no":
                            case "disable":
                            case "disabled":
                            case "false":
                                isTelemetryDisabled0 = true;
                                break;
                            case "full":
                            case "extra":
                                isTelemetryDisabled0 = false;
                                enablesFullTelemetry0 = true;
                                break;
                            case "yes":
                            case "enable":
                            case "enabled":
                            case "true":
                            case "mandatory":
                            default:
                                isTelemetryDisabled0 = false;
                                break;
                        }
                    }
                } catch (IOException e) {
                    isTelemetryDisabled0 = false;
                }
            }
        }

        if (isTelemetryDisabled0) {
            CURRENT = DISABLED;
        } else if (enablesFullTelemetry0) {
            CURRENT = FULL;
        } else {
            CURRENT = MANDATORY;
        }
    }
}
