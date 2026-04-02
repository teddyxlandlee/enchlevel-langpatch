package xland.mcmod.enchlevellangpatch.impl.telemetry;

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
            HttpPost request = new HttpPost(TELEMETRY_ENDPOINT);
            StringEntity entity = new StringEntity(data, StandardCharsets.UTF_8);
            entity.setContentType("application/json");

            request.setEntity(entity);
            client.execute(request);
        }

        return null;
    }
}
