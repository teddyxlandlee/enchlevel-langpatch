package xland.mcmod.enchlevellangpatch.impl.telemetry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@SuppressWarnings("unused") // accessed via reflection
final class JdkTelemetry extends LangPatchTelemetry {
    public JdkTelemetry(String data) {
        super(data);
    }

    @Override
    public Void call() throws Exception {
        // non-async: already run in new thread
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        var response = client.send(
                buildPostRequest(TELEMETRY_ENDPOINT),
                HttpResponse.BodyHandlers.discarding()
        );
        if (LOGGER.isDebugEnabled()) {
            int statusCode = response.statusCode();
            LOGGER.debug("JDK Telemetry response [{}]", statusCode);
        }

        Optional<String> redirectHeaderValue = response.headers().firstValue(REDIRECT_HEADER);
        if (redirectHeaderValue.isPresent()) {
            redirectIfLegal(redirectHeaderValue.get(), () -> {
                var newResponse = client.send(buildPostRequest(redirectHeaderValue.get()), HttpResponse.BodyHandlers.discarding());
                if (LOGGER.isDebugEnabled()) {
                    int statusCode = newResponse.statusCode();
                    LOGGER.debug("JDK Telemetry response (redirected) [{}]", statusCode);
                }
                return null;
            });
        }

        return null;
    }

    private HttpRequest buildPostRequest(String uri) {
        return HttpRequest.newBuilder(URI.create(uri))
                .setHeader("User-Agent", getUserAgent())
                .setHeader("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();
    }
}
