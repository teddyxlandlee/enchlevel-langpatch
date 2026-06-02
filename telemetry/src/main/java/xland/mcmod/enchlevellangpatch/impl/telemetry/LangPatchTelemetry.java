package xland.mcmod.enchlevellangpatch.impl.telemetry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public abstract class LangPatchTelemetry implements Callable<Void> {
    public static CompletableFuture<Void> ofFuture(String data) {
        return CompletableFuture.runAsync(() -> sendTelemetry(data));
    }

    private static void sendTelemetry(String data) {
        if (TelemetryConfig.getCurrent() == TelemetryConfig.DISABLED) return;

        final LangPatchTelemetry telemetry;
        if (isJava11OrLater()) {
            try {
                Class<?> c = Class.forName("xland.mcmod.enchlevellangpatch.impl.telemetry.JdkTelemetry");
                telemetry = (LangPatchTelemetry) c.getConstructor(String.class).newInstance(data);
            } catch (Exception e) {
                LOGGER.error("Failed to instantiate JdkTelemetry", e);
                return;
            }
        } else if (isApacheHttpClientAvailable()) {
            telemetry = new ApacheTelemetry(data);
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

    protected static final String TELEMETRY_ENDPOINT = "https://telem.ellp.mods.hixland.com/v2";

    @ApiStatus.Obsolete
    protected static final String REDIRECT_HEADER = "X-Entrypoint-Redirect";
    @ApiStatus.Obsolete
    protected static final Collection<String> ALLOWED_REDIRECT_HOST = Collections.emptySet();

    protected static void redirectIfLegal(@Nullable URI redirectHeaderValue, Callable<?> action) throws Exception {
        String host;
        if (redirectHeaderValue != null && (host = redirectHeaderValue.getHost()) != null &&
                ALLOWED_REDIRECT_HOST.contains(host) &&
                "https".equals(redirectHeaderValue.getScheme())
        ) {
            action.call();
        } else {
            LOGGER.warn("Illegal {} value: {}. Redirection aborted.", REDIRECT_HEADER, redirectHeaderValue);
        }
    }

    protected static void redirectIfLegal(@Nullable String redirectHeaderValue, Callable<?> action) throws Exception {
        redirectIfLegal(
                redirectHeaderValue == null ? null : URI.create(redirectHeaderValue),
                action
        );
    }

    protected String getUserAgent() {
        return "LangPatch/" +
                LangPatchTelemetry.class.getPackage().getImplementationVersion() +
                "(Client: " + getClass().getSimpleName() + ')';
    }

    @Override
    public abstract @Nullable Void call() throws Exception;
}
