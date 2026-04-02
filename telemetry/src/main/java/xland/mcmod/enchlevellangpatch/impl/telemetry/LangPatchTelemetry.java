package xland.mcmod.enchlevellangpatch.impl.telemetry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

public abstract class LangPatchTelemetry implements Callable<Void> {
    public static Thread ofThread(String data) {
        return new Thread(() -> sendTelemetry(data), "LangPatch-Telemetry");
    }

    private static void sendTelemetry(String data) {
        if (TelemetryConfig.getCurrent() == TelemetryConfig.DISABLED) return;

        final LangPatchTelemetry telemetry;
        if (isApacheHttpClientAvailable()) {
            telemetry = new ApacheTelemetry(data);
        } else if (isJava11OrLater()) {
            try {
                Class<?> c = Class.forName("xland.mcmod.enchlevellangpatch.impl.telemetry.JdkTelemetry");
                telemetry = (LangPatchTelemetry) c.getConstructor(String.class).newInstance(data);
            } catch (Exception e) {
                LOGGER.error("Failed to instantiate JdkTelemetry", e);
                return;
            }
        } else {
            LOGGER.error(
                    "Corrupted telemetry environment: no Apache httpclient found; Java version: {}",
                    System.getProperty("java.version")
            );
            return;
        }

        try {
            telemetry.call();
        } catch (Exception e) {
            LOGGER.error("Failed to send telemetry", e);
        }
    }

    private static boolean isApacheHttpClientAvailable() {
        try {
            Class.forName("org.apache.http.client.HttpClient");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean isJava11OrLater() {
        String versionString = System.getProperty("java.version");
        int versionInt = Integer.parseInt(versionString.substring(0, versionString.indexOf('.')));
        // For Java 8, this variable is 1 (from "1.8.*"), also returns false
        return versionInt >= 11;
    }

    protected final String data;
    protected static final Logger LOGGER = LogManager.getLogger();

    public LangPatchTelemetry(String data) {
        this.data = data;
    }

    protected static final String TELEMETRY_ENDPOINT = "https://telemetry.langpatch.mc.7c7.icu/api/telemetry";

    protected static String getUserAgent() {
        return "LangPatch/" + LangPatchTelemetry.class.getPackage().getImplementationVersion();
    }
}
