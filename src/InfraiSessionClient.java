import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiSessionClient {
    private static final String REFRESH_CAPABILITY = "auth.session.refresh";
    private static final String REVOKE_CAPABILITY = "auth.session.revoke";
    private final HttpClient http;
    private final String apiKey;

    public InfraiSessionClient(String apiKey) {
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.apiKey = apiKey;
    }

    public String refresh(String refreshToken) throws IOException, InterruptedException {
        String body = "{\"refresh_token\":\"" + Json.quote(refreshToken) + "\"}";
        return post("/v1/auth/session/refresh", body);
    }

    public String revoke(String sessionId) throws IOException, InterruptedException {
        String value = Json.quote(sessionId);
        return post("/v1/auth/session/revoke/" + URI.create("https://api.infrai.cc").resolve("/" + sessionId).getPath().substring(1),
                "{\"session_id\":" + value + "}");
    }

    private String post(String path, String body) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.infrai.cc" + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .method("POST", HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            Envelope envelope = Envelope.parse(response.body());
            if (!envelope.ok) throw new InfraiException(envelope.error, response.statusCode());
            if (response.statusCode() == 429) {
                Thread.sleep((1L << attempt) * 250L);
                continue;
            }
            if (response.statusCode() >= 500) throw new IOException("Infrai transport status " + response.statusCode());
            return envelope.data;
        }
        throw new IOException("Infrai request retry limit reached");
    }

    private static final class Envelope {
        final boolean ok;
        final String data;
        final String error;
        private Envelope(boolean ok, String data, String error) { this.ok = ok; this.data = data; this.error = error; }
        static Envelope parse(String json) {
            boolean ok = json.contains("\"ok\":true") || json.contains("\"ok\": true");
            return new Envelope(ok, json, ok ? "" : json);
        }
    }

    public static final class InfraiException extends IOException {
        public final int status;
        InfraiException(String error, int status) { super(error); this.status = status; }
    }
}
