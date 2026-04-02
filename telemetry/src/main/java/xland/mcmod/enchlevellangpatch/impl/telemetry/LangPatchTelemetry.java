package xland.mcmod.enchlevellangpatch.impl.telemetry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public abstract class LangPatchTelemetry implements Callable<Void> {
    public static CompletableFuture<Void> ofFuture(String data) {
//        return new Thread(() -> sendTelemetry(data), "LangPatch-Telemetry");
        return CompletableFuture.runAsync(() -> sendTelemetry(data));
    }

    private static void sendTelemetry(String data) {
        if (TelemetryConfig.getCurrent() == TelemetryConfig.DISABLED) return;

        final LangPatchTelemetry telemetry;
        if (isApacheHttpClientAvailable()) {
            telemetry = new ApacheTelemetry(data);
        } else if (isJava16OrLater()) {
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

    private static boolean isJava16OrLater() {
        String versionString = System.getProperty("java.version");
        int versionInt = Integer.parseInt(versionString.substring(0, versionString.indexOf('.')));
        // For Java 8, this variable is 1 (from "1.8.*"), also returns false
        return versionInt >= 16;
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
