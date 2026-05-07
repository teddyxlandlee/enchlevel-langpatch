package xland.mcmod.enchlevellangpatch.impl.telemetry;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.nio.charset.StandardCharsets;

final class ApacheTelemetry extends LangPatchTelemetry {
    ApacheTelemetry(String data) {
        super(data);
    }

    @Override
    public Void call() throws Exception {
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setUserAgent(getUserAgent())
                .build()) {
            HttpPost request = buildJsonPostRequest(TELEMETRY_ENDPOINT);
            CloseableHttpResponse response = client.execute(request);
            if (LOGGER.isDebugEnabled()) {
                int statusCode = response.getStatusLine().getStatusCode();
                LOGGER.debug("Telemetry response code: {}", statusCode);
            }

            Header redirectHeader = request.getFirstHeader(REDIRECT_HEADER);
            if (redirectHeader != null) {
                redirectIfLegal(redirectHeader.getValue(), () -> doRedirect(client, redirectHeader.getValue()));
            }
        }

        return null;
    }

    private Void doRedirect(CloseableHttpClient client, String uri) throws Exception {
        HttpPost request = buildJsonPostRequest(uri);
        CloseableHttpResponse response = client.execute(request);
        if (LOGGER.isDebugEnabled()) {
            int statusCode = response.getStatusLine().getStatusCode();
            LOGGER.debug("Telemetry response code (redirected): {}", statusCode);
        }
        return null;
    }

    private HttpPost buildJsonPostRequest(String uri) {
        HttpPost request = new HttpPost(uri);
        StringEntity entity = new StringEntity(data, StandardCharsets.UTF_8);
        entity.setContentType("application/json");

        request.setEntity(entity);
        return request;
    }
}
