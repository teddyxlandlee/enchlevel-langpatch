package xland.mcmod.enchlevellangpatch.impl.telemetry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SuppressWarnings("unused") // accessed via reflection
final class JdkTelemetry extends LangPatchTelemetry {
    public JdkTelemetry(String data) {
        super(data);
    }

    @Override
    public Void call() throws Exception {
        // non-async: already run in new thread
        HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(TELEMETRY_ENDPOINT))
                        .setHeader("User-Agent", getUserAgent())
                        .setHeader("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(data))
                        .build(),
                HttpResponse.BodyHandlers.discarding()
        );

        return null;
    }
}
